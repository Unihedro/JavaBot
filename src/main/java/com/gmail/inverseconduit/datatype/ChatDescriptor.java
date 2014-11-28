package com.gmail.inverseconduit.datatype;

public class ChatDescriptor {

    protected final ProviderDescriptor provider;

    protected final RoomDescriptor     room;

    ChatDescriptor(DescriptorBuilder builder) {
        provider = builder.getProvider();
        room = builder.getRoom();
    }

    public static class DescriptorBuilder {

        private final ProviderDescriptor provider;

        private RoomDescriptor           room;

        public DescriptorBuilder(ProviderDescriptor provider) {
            this.provider = provider;
        }

        public ChatDescriptor build() {
            return new ChatDescriptor(this);
        }

        private ProviderDescriptor getProvider() {
            return provider;
        }

        private RoomDescriptor getRoom() {
            return room;
        }

        public DescriptorBuilder setRoom(RoomDescriptor room) {
            this.room = room;
            return this;
        }
    }

    public ProviderDescriptor getProvider() {
        return provider;
    }

    public RoomDescriptor getRoom() {
        return room;
    }
}
