package com.gmail.inverseconduit.datatype;

public final class SeChatDescriptor extends ChatDescriptor {

    public String buildRestRootUrl() {
        return provider.getDescription().toString() + String.format("/chats/%d/", (Integer) room.getRoomDescription());
    }

    public String buildRoomUrl() {
        return provider.getDescription().toString() + String.format("/rooms/%d/", (Integer) room.getRoomDescription());
    }

    private SeChatDescriptor(DescriptorBuilder builder) {
        super(new ChatDescriptor.DescriptorBuilder(builder.provider).setRoom(builder.room));
    }

    public static class DescriptorBuilder {

        private final ProviderDescriptor provider;

        private RoomDescriptor           room;

        public DescriptorBuilder(ProviderDescriptor provider) {
            this.provider = provider;
        }

        public SeChatDescriptor build() {
            return new SeChatDescriptor(this);
        }

        public DescriptorBuilder setRoom(RoomDescriptor room) {
            this.room = room;
            return this;
        }
    }
}
