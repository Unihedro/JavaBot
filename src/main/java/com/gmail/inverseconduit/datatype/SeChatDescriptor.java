package com.gmail.inverseconduit.datatype;

public final class SeChatDescriptor extends ChatDescriptor implements Comparable {

    public static SeChatDescriptor buildSeChatDescriptorFrom(ChatMessage msg) {
        return new SeChatDescriptor.DescriptorBuilder(msg.getSite()).setRoom(() -> msg.getRoomId()).build();
    }

    public String buildRestRootUrl() {
        return provider.getDescription().toString() + String.format("chats/%d/", (Integer) room.getRoomDescription());
    }

    public String buildRoomUrl() {
        return provider.getDescription().toString() + String.format("" + "rooms/%d/", (Integer) room.getRoomDescription());
    }

    private SeChatDescriptor(DescriptorBuilder builder) {
        super(new ChatDescriptor.DescriptorBuilder(builder.provider).room(builder.room));
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

    @Override
    public int compareTo(Object o) {
        SeChatDescriptor other = (SeChatDescriptor) o;
        if ( !provider.getDescription().equals(other.getProvider().getDescription())) { return -1; }
        if ( !room.getRoomDescription().equals(other.getRoom().getRoomDescription())) { return 1; }
        return 0;

    }
}
