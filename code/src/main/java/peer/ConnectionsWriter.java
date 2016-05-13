package peer;

import peer.actions.Action;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsWriter implements Runnable {

    final private ConcurrentHashMap<Long, Action> actions;

    final private Selector writeSelector;
    final private ByteBuffer interimWriteBuffer;


    public ConnectionsWriter() throws IOException {
        this.actions = new ConcurrentHashMap<>();
        this.writeSelector = Selector.open();
        this.interimWriteBuffer = ByteBuffer.allocateDirect(Constants.BYTE_BUFFER_SIZE);
    }

    @Override
    public void run() {
        try {
            if (this.writeSelector.selectNow() > 0) {
                Set<SelectionKey> selectionKeys = this.writeSelector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    Action action = (Action) selectionKey.attachment();
                    if (action.execute(this.interimWriteBuffer)) {
                        this.actions.remove(action.getConnection().getId());
                        selectionKey.cancel();
                    }
                    keyIterator.remove();
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(Action action) throws ClosedChannelException {
        Connection connection = action.getConnection();
        this.actions.put(connection.getId(), action);
        connection.register(this.writeSelector, SelectionKey.OP_WRITE, action);
    }
}
