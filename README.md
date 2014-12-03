<h1>What this is:</h1>

Well it's relatively simple.. A bot. For the Stack Exchange chat.
It can read and post messages and understands a few commands.

Currently the Bot is under development.

<h1>How do I get it:</h1>

You can simply pull the repository on your local machine, 
import the maven project and already run it.

<h1>What do I need:</h1>

You need: Apache Maven, Java 1.8, 
and a file named `bot.properties`, as well as an account on any Stack Exchange site (or Stack Overflow and Meta Stack Exchange as the reputation points do not inherit from network rep on these two domains), having at least 20 reputation.

<h2>Running the bot:</h2>

It's dead simple, you grab the repository, and create a file named `bot.properties`
in the project root containing your configuration. 

The `bot.properties` file must contain following property configurations:

    TRIGGER={ExampleTrigger}
    LOGIN-EMAIL={Stackexchange login email}
    PASSWORD={Stackexchange login password}

Make sure there are no excess spaces in the `.properties` file.

The next thing you want to do is run `mvn clean compile assembly:single` to pack the bot into a runnable jar.
Take the runnable wherever you want it to go, and take the bot.properties with you.

Running the bot is as simple as `java -jar javabot_0.0.1-jar-with-dependencies.jar`
