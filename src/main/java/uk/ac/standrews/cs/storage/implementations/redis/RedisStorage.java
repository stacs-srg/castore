package uk.ac.standrews.cs.storage.implementations.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import uk.ac.standrews.cs.storage.data.Data;
import uk.ac.standrews.cs.storage.exceptions.DestroyException;
import uk.ac.standrews.cs.storage.exceptions.StorageException;
import uk.ac.standrews.cs.storage.implementations.CommonStorage;
import uk.ac.standrews.cs.storage.interfaces.IDirectory;
import uk.ac.standrews.cs.storage.interfaces.IFile;
import uk.ac.standrews.cs.storage.interfaces.IStorage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * TODO - set the SAVE configuration when creating the redis instance?
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class RedisStorage extends CommonStorage implements IStorage {

    private static Logger log = Logger.getLogger(RedisStorage.class.getName());

    private final static int DEFAULT_PORT = 6379;
    private final static String PING_RESPONSE = "PONG";

    private JedisPool pool;
    private String hostname;
    private int port;

    public RedisStorage(String hostname) {
        this(hostname, DEFAULT_PORT);
    }

    /**
     * Note: You should make sure that a redis instance is available
     *
     * @param hostname
     */
    public RedisStorage(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        pool = new JedisPool(new JedisPoolConfig(), hostname, port);

        try(Jedis jedis = pool.getResource()) {

            String response = jedis.ping();
            if (response.equals(PING_RESPONSE)) {
                createRoot(jedis);
            } else {
                log.log(Level.SEVERE, "Redis Storage could not be created because there is not an available or reachable Redis server");
            }

        }

    }

    @Override
    public IDirectory createDirectory(IDirectory parent, String name) throws StorageException {
        try(Jedis jedis = pool.getResource()) {
            return new RedisDirectory(jedis, parent, name);
        }
    }

    @Override
    public IDirectory createDirectory(String name) throws StorageException {
        return createDirectory(root, name);
    }

    @Override
    public IFile createFile(IDirectory parent, String filename) throws StorageException {
        try(Jedis jedis = pool.getResource()) {
            return new RedisFile(jedis, parent, filename);
        }
    }

    @Override
    public IFile createFile(IDirectory parent, String filename, Data data) throws StorageException {
        try(Jedis jedis = pool.getResource()) {
            return new RedisFile(jedis, parent, filename, data);
        }
    }

    @Override
    public void persist() {
        try(Jedis jedis = pool.getResource()) {
            jedis.bgsave();
        }
    }

    @Override
    public void destroy() throws DestroyException {
        try(Jedis jedis = pool.getResource()) {
            jedis.flushDB();
        }

        pool.destroy();
    }

    private void createRoot(Jedis jedis) {
        root = new RedisDirectory(jedis, hostname);
    }
}
