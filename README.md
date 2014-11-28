<h1>What this is:</h1>

Well it's relatively simple.. A bot. For the stackexchange chat.
It can read and post messages and understands a few commands.

Currently the Bot is under development.

<h1>How do I get it:</h1>

You can simply pull the repository on your local machine, 
import the maven project and already run it.

<h1>What do I need:</h1>

You need: Apache Maven, Java 1.8, 
and a file named bot.properties, as well as an account at any StackExchange site, having 20 (or more) reputation

<h2>Running the bot:</h2>

It's dead simple, you grab the repository, create a file named bot.properties
in the project root containing your configuration. 

The bot.properties must contain following property configurations:

´´´
TRIGGER={ExampleTrigger}
LOGIN-EMAIL={Stackexchange login email}
PASSWORD={Stackexchange login password}
´´´

Make sure there's no excess spaces in the properties file

The next thing you want to do is run `mvn clean compile assembly:single` to pack the bot into a runnable jar.
Take the runnable wherever you want it to go, and take the bot.properties with you.

Running the bot is as simple as `java -jar javabot_0.0.1-jar-with-dependencies.jar`
