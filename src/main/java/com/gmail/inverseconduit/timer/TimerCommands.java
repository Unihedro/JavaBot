package com.gmail.inverseconduit.timer;

import static com.gmail.inverseconduit.AppContext.INSTANCE;

import com.gmail.inverseconduit.chat.ChatInterface;
import com.gmail.inverseconduit.commands.CommandHandle;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.datatype.SeChatDescriptor;

public final class TimerCommands {

    private static final String DEFAULT_MESSAGE = "Time's up!";

    public static CommandHandle timerCommand(ChatInterface chatInterface) {
        CommandHandle handle =
                new CommandHandle.Builder("timer", message -> TimerCommands.processChatMessage(message, chatInterface))
                        .setHelpText(
                                "Schedule Timers for your personal use. Syntax: timer delay(in minutes) (optional message). You can also abort timers by calling timer abort timernumber")
                        .setInfoText("Schedule Timers for your personal use.").build();
        return handle;
    }

    private static String processChatMessage(ChatMessage message, ChatInterface chatInterface) {
        String[] arguments = message.getMessage().split(" ");
        if (2 > arguments.length || 3 < arguments.length) { return "Command does not match the syntax: timer delay(in minutes) (optional message). You can also abort timers by calling timer abort timernumber"; }

        if (arguments[1].equalsIgnoreCase("abort") && 3 == arguments.length) { return handleCancelRequest(message, chatInterface, arguments); }
        int requestedDelay;
        try {
            requestedDelay = Integer.parseInt(arguments[1]);
        } catch(NumberFormatException ex) {
            return "Requested delay was not a number :(";
        }

        final int timernumber;
        if (3 == arguments.length) { // includes a custom message
            timernumber = INSTANCE.get(TimerKeeper.class).addTimer(() -> {
                chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), String.format("@%s %s", message.getUsername(), arguments[2]));
            }, requestedDelay);
        }
        else {
            timernumber = INSTANCE.get(TimerKeeper.class).addTimer(() -> {
                chatInterface.sendMessage(SeChatDescriptor.buildSeChatDescriptorFrom(message), String.format("@%s %s", message.getUsername(), DEFAULT_MESSAGE));
            }, requestedDelay);
        }
        String result = String.format("successfully scheduled timer #%d", timernumber);
        return result;
    }

    private static String handleCancelRequest(ChatMessage message, ChatInterface chatInterface, String[] arguments) {
        int timerToCancel;
        try {
            timerToCancel = Integer.parseInt(arguments[2]);
        } catch(NumberFormatException ex) {
            return "Timernumber was not a number :(";
        }
        return INSTANCE.get(TimerKeeper.class).cancelTimer(timerToCancel);
    }
}
