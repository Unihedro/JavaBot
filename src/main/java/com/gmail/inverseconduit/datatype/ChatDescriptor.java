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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( (provider == null)
            ? 0
            : provider.hashCode());
        result = prime * result + ( (room == null)
            ? 0
            : room.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChatDescriptor other = (ChatDescriptor) obj;
        if (provider == null) {
            if (other.provider != null)
                return false;
        }
        else if ( !provider.equals(other.provider))
            return false;
        if (room == null) {
            if (other.room != null)
                return false;
        }
        else if ( !room.equals(other.room))
            return false;
        return true;
    }

    public RoomDescriptor getRoom() {
        return room;
    }
}
