package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.utils.ChatBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * Generates chat message responses to the "javadoc" command. Also generates
 * responses to numeric chat messages that users enter when the javadoc command
 * presents them with a list of choices.
 */
public class JavaDocAccessor {
	private final JavadocDao dao;

	/**
	 * 
	 */
	private List<String> prevChoices = new ArrayList<>();
	private long prevChoicesPinged = 0;
	private final long choiceTimeout = TimeUnit.SECONDS.toMillis(30);

	/**
	 * "Flags" that a class can have. They are defined in a List because, if a
	 * class has multiple modifiers, I want them to be displayed in a consistent
	 * order.
	 */
	private static final List<String> classModifiers;
	static {
		ImmutableList.Builder<String> b = new ImmutableList.Builder<>();
		b.add("abstract", "final");
		classModifiers = b.build();
	}

	/**
	 * The list of possible "class types". Each class *should* have exactly one
	 * type, but there's no explicit check for this (things shouldn't break if a
	 * class does not have exactly one).
	 */
	private static final Set<String> classTypes;
	static {
		ImmutableSet.Builder<String> b = new ImmutableSet.Builder<>();
		b.add("annotation", "class", "enum", "exception", "interface");
		classTypes = b.build();
	}

	/**
	 * The method modifiers to ignore when outputting a method to the chat.
	 */
	private static final Set<String> methodModifiersToIgnore;
	static {
		ImmutableSet.Builder<String> b = new ImmutableSet.Builder<>();
		b.add("private", "protected", "public");
		methodModifiersToIgnore = b.build();
	}

	/**
	 * @param dir the directory to the Javadocs folder
	 * @throws IOException if there's a problem reading a Javadoc file
	 */
	public JavaDocAccessor(Path dir) throws IOException {
		dao = new JavadocDao(dir);
	}

	/**
	 * Processes a Javadoc command.
	 * @param chatMessage the chat message (e.g. "**javadoc: arraylist")
	 * @param commandText the command text (e.g. "arraylist")
	 * @return the chat response
	 */
	public String javadoc(ChatMessage chatMessage, String commandText) {
		String response = generateResponse(commandText.trim());

		ChatBuilder cb = new ChatBuilder();
		cb.reply(chatMessage);
		cb.append(response);
		return cb.toString();
	}

	private String generateResponse(String commandTextStr) {
		if (commandTextStr.isEmpty()) {
			return "Type the name of a Java class (e.g. \"java.lang.String\") or a method (e.g. \"Integer#parseInt\").";
		}

		//parse the command
		CommandTextParser commandText = new CommandTextParser(commandTextStr);

		ClassInfo info;
		try {
			info = dao.getClassInfo(commandText.className);
		} catch (MultipleClassesFoundException e) {
			if (commandText.methodName == null) {
				return printClassChoices(e.getClasses());
			}

			//search each class for a method that matches the given signature
			Map<ClassInfo, MethodInfo> exactMatches = new HashMap<>();
			Multimap<ClassInfo, MethodInfo> matchingNames = ArrayListMultimap.create();
			for (String className : e.getClasses()) {
				try {
					ClassInfo classInfo = dao.getClassInfo(className);
					MatchingMethods methods = getMatchingMethods(classInfo, commandText.methodName, commandText.parameters);
					if (methods.exactSignature != null) {
						exactMatches.put(classInfo, methods.exactSignature);
					}
					matchingNames.putAll(classInfo, methods.matchingName);
				} catch (IOException e2) {
					throw new RuntimeException("Problem getting Javadoc info.", e2);
				}
			}

			if (exactMatches.isEmpty() && matchingNames.isEmpty()) {
				//no matches found
				return "Sorry, I can't find that method. :(";
			}

			if (exactMatches.size() == 1) {
				//a single, exact match was found!
				MethodInfo method = exactMatches.values().iterator().next();
				ClassInfo classInfo = exactMatches.keySet().iterator().next();
				return printMethod(method, classInfo, commandText.paragraph);
			}

			//multiple matches were found
			Multimap<ClassInfo, MethodInfo> map;
			if (exactMatches.size() > 1) {
				map = ArrayListMultimap.create();
				for (Map.Entry<ClassInfo, MethodInfo> entry : exactMatches.entrySet()) {
					map.put(entry.getKey(), entry.getValue());
				}
			} else {
				map = matchingNames;
			}
			return printMethodChoices(map, commandText.parameters);
		} catch (IOException e) {
			throw new RuntimeException("Problem getting Javadoc info.", e);
		}

		if (info == null) {
			//couldn't find the class
			return "Sorry, I never heard of that class. :(";
		}

		if (commandText.methodName == null) {
			//method name not specified, so print the class docs
			return printClass(info, commandText.paragraph);
		}

		//print the method docs
		MatchingMethods matchingMethods;
		try {
			matchingMethods = getMatchingMethods(info, commandText.methodName, commandText.parameters);
		} catch (IOException e) {
			throw new RuntimeException("Problem getting Javadoc info.", e);
		}

		if (matchingMethods.isEmpty()) {
			//no matches found
			return "Sorry, I can't find that method. :(";
		}

		if (matchingMethods.exactSignature != null) {
			//an exact match was found!
			return printMethod(matchingMethods.exactSignature, info, commandText.paragraph);
		}

		if (matchingMethods.matchingName.size() == 1 && commandText.parameters == null) {
			return printMethod(matchingMethods.matchingName.get(0), info, commandText.paragraph);
		}

		//print the methods with the same name
		Multimap<ClassInfo, MethodInfo> map = ArrayListMultimap.create();
		map.putAll(info, matchingMethods.matchingName);
		return printMethodChoices(map, commandText.parameters);
	}

	/**
	 * Called when someone types a number into the chat, in response to the
	 * javadoc command showing a list of choices.
	 * @param message the chat message
	 * @param num the number
	 * @return the chat response or null to ignore the message
	 */
	public String showChoice(ChatMessage message, int num) {
		if (prevChoicesPinged == 0) {
			return null;
		}

		boolean timedOut = System.currentTimeMillis() - prevChoicesPinged > choiceTimeout;
		if (timedOut) {
			return null;
		}

		//reset the time-out timer
		prevChoicesPinged = System.currentTimeMillis();

		int index = num - 1;
		if (index < 0 || index >= prevChoices.size()) {
			ChatBuilder cb = new ChatBuilder();
			cb.reply(message).append("That's not a valid choice.");
			return cb.toString();

		}

		String msg = prevChoices.get(index);
		return javadoc(message, msg);
	}

	/**
	 * Prints the Javadoc info of a particular method.
	 * @param methodinfo the method
	 * @param classInfo the class that the method belongs to
	 * @param paragraph the paragraph to print
	 * @return the chat response
	 */
	private String printMethod(MethodInfo methodInfo, ClassInfo classInfo, int paragraph) {
		ChatBuilder cb = new ChatBuilder();
		if (paragraph == 1) {
			//print library name
			LibraryZipFile zipFile = classInfo.getZipFile();
			if (zipFile != null) {
				String name = zipFile.getName();
				if (name != null && !name.equalsIgnoreCase("java")) {
					name = name.replace(' ', '-');
					cb.bold();
					cb.tag(name);
					cb.bold();
					cb.append(' ');
				}
			}

			//print modifiers
			boolean deprecated = methodInfo.isDeprecated();
			Collection<String> modifiersToPrint = new ArrayList<String>(methodInfo.getModifiers());
			modifiersToPrint.removeAll(methodModifiersToIgnore);
			for (String modifier : modifiersToPrint) {
				if (deprecated) cb.strike();
				cb.tag(modifier);
				if (deprecated) cb.strike();
				cb.append(' ');
			}

			//print signature
			if (deprecated) cb.strike();
			String signature = methodInfo.getSignatureString();
			String url = classInfo.getUrl();
			if (url == null) {
				cb.bold().code(signature).bold();
			} else {
				url += "#" + methodInfo.getUrlAnchor();
				cb.link(new ChatBuilder().bold().code(signature).bold().toString(), url);
			}
			if (deprecated) cb.strike();
			cb.append(": ");
		}

		//print the method description
		String description = methodInfo.getDescription();
		Paragraphs paragraphs = new Paragraphs(description);
		paragraphs.append(paragraph, cb);
		return cb.toString();
	}

	/**
	 * Prints the methods to choose from when multiple methods are found.
	 * @param matchingMethods the methods to choose from
	 * @param methodParams the parameters of the method or null if no parameters
	 * were specified
	 * @return the chat response
	 */
	private String printMethodChoices(Multimap<ClassInfo, MethodInfo> matchingMethods, List<String> methodParams) {
		prevChoices = new ArrayList<>();
		prevChoicesPinged = System.currentTimeMillis();
		ChatBuilder cb = new ChatBuilder();
		cb.append(buildMethodChoiceQuestion(matchingMethods, methodParams));

		int count = 1;
		for (Map.Entry<ClassInfo, MethodInfo> entry : matchingMethods.entries()) {
			ClassInfo classInfo = entry.getKey();
			MethodInfo methodInfo = entry.getValue();

			String signature;
			{
				StringBuilder sb = new StringBuilder();
				sb.append(classInfo.getName().getFullyQualified()).append("#").append(methodInfo.getName());

				List<String> paramList = new ArrayList<>();
				for (ParameterInfo param : methodInfo.getParameters()) {
					paramList.add(param.getType().getSimple() + (param.isArray() ? "[]" : ""));
				}
				sb.append('(').append(String.join(", ", paramList)).append(')');

				signature = sb.toString();
			}

			cb.nl().append(count + "").append(". ").append(signature);
			prevChoices.add(signature);
			count++;
		}
		return cb.toString();
	}

	private String buildMethodChoiceQuestion(Multimap<ClassInfo, MethodInfo> matchingMethods, List<String> methodParams) {
		if (matchingMethods.size() == 1) {
			if (methodParams == null) {
				return "Did you mean this one? (type the number)";
			}

			if (methodParams.isEmpty()) {
				return "I couldn't find a zero-arg signature for that method.";
			}

			//@formatter:off
			return
			"I couldn't find a signature with " +
			((methodParams.size() == 1) ? "that parameter." : "those parameters.") + 
			" Did you mean this one? (type the number)";
			//@formatter:on
		}

		if (methodParams == null) {
			return "Which one do you mean? (type the number)";
		}

		StringBuilder sb = new StringBuilder();
		if (methodParams.isEmpty()) {
			sb.append("I couldn't find a zero-arg signature for that method.");
		} else {
			sb.append("I couldn't find a signature with ");
			sb.append((methodParams.size() == 1) ? "that parameter." : "those parameters.");
		}
		sb.append(" Did you mean one of these? (type the number)");
		return sb.toString();
	}

	/**
	 * Prints the classes to choose from when multiple class are found.
	 * @param classes the fully-qualified names of the classes
	 * @return the chat response
	 */
	private String printClassChoices(Collection<String> classes) {
		List<String> choices = new ArrayList<>(classes);
		Collections.sort(choices);
		prevChoices = choices;
		prevChoicesPinged = System.currentTimeMillis();

		ChatBuilder cb = new ChatBuilder();
		cb.append("Which one do you mean? (type the number)");

		int count = 1;
		for (String name : choices) {
			cb.nl().append(count + "").append(". ").append(name);
			count++;
		}

		return cb.toString();
	}

	/**
	 * Prints the description of a class.
	 * @param info the class info
	 * @param paragraph the paragraph to print
	 * @return the chat response
	 */
	private String printClass(ClassInfo info, int paragraph) {
		ChatBuilder cb = new ChatBuilder();
		if (paragraph == 1) {
			//print the library name
			LibraryZipFile zipFile = info.getZipFile();
			if (zipFile != null) {
				String name = zipFile.getName();
				if (name != null && !name.equalsIgnoreCase("Java")) {
					name = name.replace(" ", "-");
					cb.bold();
					cb.tag(name);
					cb.bold();
					cb.append(' ');
				}
			}

			//print modifiers
			boolean deprecated = info.isDeprecated();
			Collection<String> infoModifiers = info.getModifiers();
			List<String> modifiersToPrint = new ArrayList<>(classModifiers);
			modifiersToPrint.retainAll(infoModifiers);

			//add class modifiers
			for (String classModifier : modifiersToPrint) {
				cb.italic();
				if (deprecated) cb.strike();
				cb.tag(classModifier);
				if (deprecated) cb.strike();
				cb.italic();
				cb.append(' ');
			}

			Collection<String> classType = new HashSet<>(classTypes);
			classType.retainAll(infoModifiers);
			//there should be only one remaining element in the collection, but use a foreach loop just incase
			for (String modifier : classType) {
				if (deprecated) cb.strike();
				cb.tag(modifier);
				if (deprecated) cb.strike();
				cb.append(' ');
			}

			//print class name
			if (deprecated) cb.strike();
			String fullName = info.getName().getFullyQualified();
			String url = info.getFrameUrl();
			if (url == null) {
				cb.bold().code(fullName).bold();
			} else {
				cb.link(new ChatBuilder().bold().code(fullName).bold().toString(), url);
			}
			if (deprecated) cb.strike();
			cb.append(": ");
		}

		//print the class description
		String description = info.getDescription();
		Paragraphs paragraphs = new Paragraphs(description);
		paragraphs.append(paragraph, cb);
		return cb.toString();
	}

	/**
	 * Finds the methods in a given class that matches the given method
	 * signature.
	 * @param info the class to search
	 * @param methodName the name of the method to search for
	 * @param methodParameters the parameters that the method should have, or
	 * null not to look at the parameters.
	 * @return the matching methods
	 * @throws IOException if there's a problem loading Javadoc info
	 */
	private MatchingMethods getMatchingMethods(ClassInfo info, String methodName, List<String> methodParameters) throws IOException {
		MatchingMethods matchingMethods = new MatchingMethods();
		Set<String> matchingNameSignatures = new HashSet<>();

		//search the class, all its parent classes, and all its interfaces and the interfaces of its super classes
		LinkedList<ClassInfo> typeStack = new LinkedList<>();
		typeStack.add(info);

		while (!typeStack.isEmpty()) {
			ClassInfo curInfo = typeStack.removeLast();
			for (MethodInfo curMethod : curInfo.getMethods()) {
				if (!curMethod.getName().equalsIgnoreCase(methodName)) {
					//name doesn't match
					continue;
				}

				String signature = curMethod.getSignature();
				if (matchingNameSignatures.contains(signature)) {
					//this method is already in the matching name list
					continue;
				}

				matchingNameSignatures.add(signature);
				matchingMethods.matchingName.add(curMethod);

				if (methodParameters == null) {
					//user is not searching based on parameters
					continue;
				}

				List<ParameterInfo> curParameters = curMethod.getParameters();
				if (curParameters.size() != methodParameters.size()) {
					//parameter size doesn't match
					continue;
				}

				//check the parameters
				boolean exactMatch = true;
				for (int i = 0; i < curParameters.size(); i++) {
					ParameterInfo curParameter = curParameters.get(i);
					String curParameterName = curParameter.getType().getSimple() + (curParameter.isArray() ? "[]" : "");

					String methodParameter = methodParameters.get(i);

					if (!curParameterName.equalsIgnoreCase(methodParameter)) {
						//parameter types don't match
						exactMatch = false;
						break;
					}
				}
				if (exactMatch) {
					matchingMethods.exactSignature = curMethod;
				}
			}

			//add parent class to the stack
			ClassName superClass = curInfo.getSuperClass();
			if (superClass != null) {
				ClassInfo superClassInfo = dao.getClassInfo(superClass.getFullyQualified());
				if (superClassInfo != null) {
					typeStack.add(superClassInfo);
				}
			}

			//add interfaces to the stack
			for (ClassName interfaceName : curInfo.getInterfaces()) {
				ClassInfo interfaceInfo = dao.getClassInfo(interfaceName.getFullyQualified());
				if (interfaceInfo != null) {
					typeStack.add(interfaceInfo);
				}
			}
		}

		return matchingMethods;
	}

	/**
	 * Parses a javadoc chat command (e.g. "arraylist#add")
	 */
	static class CommandTextParser {
		private final static Pattern messageRegex = Pattern.compile("(.*?)(\\((.*?)\\))?(#(.*?)(\\((.*?)\\))?)?(\\s+(.*?))?$");

		private final String className, methodName;
		private final List<String> parameters;
		private final int paragraph;

		/**
		 * @param message the command text
		 */
		public CommandTextParser(String message) {
			Matcher m = messageRegex.matcher(message);
			m.find();

			className = m.group(1);

			if (m.group(2) != null) { //e.g. java.lang.string(string, string)
				int dot = className.lastIndexOf('.');
				String simpleName = (dot < 0) ? className : className.substring(dot + 1);
				methodName = simpleName;
			} else {
				methodName = m.group(5); //e.g. java.lang.string#indexOf(int)
			}

			String parametersStr = m.group(4); //e.g. java.lang.string(string, string)
			if (parametersStr == null || parametersStr.startsWith("#")) {
				parametersStr = m.group(7); //e.g. java.lang.string#string(string, string)
				if (parametersStr == null) {
					parametersStr = m.group(3);
				}
			}
			if (parametersStr == null || parametersStr.equals("*")) {
				parameters = null;
			} else if (parametersStr.isEmpty()) {
				parameters = Collections.emptyList();
			} else {
				parameters = Arrays.asList(parametersStr.split("\\s*,\\s*"));
			}

			int paragraph;
			try {
				paragraph = Integer.parseInt(m.group(9));
				if (paragraph < 1) {
					paragraph = 1;
				}
			} catch (NumberFormatException e) {
				paragraph = 1;
			}
			this.paragraph = paragraph;
		}

		public String getClassName() {
			return className;
		}

		public String getMethodName() {
			return methodName;
		}

		public List<String> getParameters() {
			return parameters;
		}

		public int getParagraph() {
			return paragraph;
		}
	}

	private static class MatchingMethods {
		private MethodInfo exactSignature;
		private final List<MethodInfo> matchingName = new ArrayList<>();

		public boolean isEmpty() {
			return exactSignature == null && matchingName.isEmpty();
		}
	}

	private static class Paragraphs {
		private final String paragraphs[];

		public Paragraphs(String text) {
			paragraphs = text.split("\n\n");
		}

		public int count() {
			return paragraphs.length;
		}

		public String get(int num) {
			return paragraphs[num - 1];
		}

		/**
		 * Appends a paragraph to a {@link ChatBuilder}.
		 * @param num the paragraph number
		 * @param cb the chat builder
		 */
		public void append(int num, ChatBuilder cb) {
			if (num > count()) {
				num = count();
			}

			cb.append(get(num));
			if (count() > 1) {
				cb.append(" (").append(num + "").append("/").append(count() + "").append(")");
			}
		}
	}
}
