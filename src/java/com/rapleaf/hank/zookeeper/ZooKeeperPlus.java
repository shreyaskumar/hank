package com.rapleaf.hank.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ZooKeeperPlus extends ZooKeeper {

  private static final List<ACL> DEFAULT_ACL = Ids.OPEN_ACL_UNSAFE;
  private static final CreateMode DEFAULT_CREATE_MODE = CreateMode.PERSISTENT;

  public ZooKeeperPlus(String connectString,
                       int sessionTimeout,
                       Watcher watcher,
                       long sessionId,
                       byte[] sessionPasswd) throws IOException {
    super(connectString, sessionTimeout, watcher, sessionId, sessionPasswd);
  }

  public ZooKeeperPlus(String connectString, int sessionTimeout, Watcher watcher) throws IOException {
    super(connectString, sessionTimeout, watcher);
  }

  public void create(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
    create(path, data, DEFAULT_ACL, createMode);
  }

  public void create(String path, byte[] data) throws KeeperException, InterruptedException {
    create(path, data, DEFAULT_ACL, DEFAULT_CREATE_MODE);
  }

  public void createLong(String path, long value) throws KeeperException, InterruptedException {
    create(path, (Long.toString(value)).getBytes(), DEFAULT_ACL, DEFAULT_CREATE_MODE);
  }

  public void createInt(String path, int value) throws KeeperException, InterruptedException {
    create(path, (Integer.toString(value)).getBytes(), DEFAULT_ACL, DEFAULT_CREATE_MODE);
  }

  public Integer getIntOrNull(String path) throws KeeperException, InterruptedException {
    Long lvalue = getLongOrNull(path);
    if (lvalue == null) {
      return null;
    }
    return lvalue.intValue();
  }

  public int getInt(String path) throws KeeperException, InterruptedException {
    return Integer.parseInt(new String(getData(path, false, new Stat())));
  }

  public void setString(String path, String value) throws KeeperException, InterruptedException {
    setData(path, value.getBytes(), -1);
  }

  public void setInt(String path, int nextVersion) throws KeeperException, InterruptedException {
    setData(path, (Integer.toString(nextVersion)).getBytes(), -1);
  }

  public long getLong(String path) throws KeeperException, InterruptedException {
    return Long.parseLong(new String(getData(path, false, new Stat())));
  }

  public Long getLongOrNull(String path) throws KeeperException, InterruptedException {
    if (exists(path, false) == null) {
      return null;
    } else {
      return Long.parseLong(new String(getData(path, false, new Stat())));
    }
  }

  public String getString(String path) throws KeeperException, InterruptedException {
    try {
      byte[] data = getData(path, false, null);
      if (data == null) {
        return null;
      }
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteIfExists(String path) throws KeeperException, InterruptedException {
    if (exists(path, false) != null) {
      delete(path, -1);
    }
  }

  public void setOrCreate(String path, int value, CreateMode createMode) throws KeeperException, InterruptedException {
    setOrCreate(path, Integer.toString(value), createMode);
  }

  public void setOrCreate(String path, long value, CreateMode createMode) throws KeeperException, InterruptedException {
    setOrCreate(path, Long.toString(value), createMode);
  }

  public void setOrCreate(String path, String value, CreateMode createMode) throws KeeperException, InterruptedException {
    if (exists(path, false) == null) {
      create(path, value.getBytes(), DEFAULT_ACL, createMode);
    } else {
      setData(path, value.getBytes(), -1);
    }
  }

  public void deleteNodeRecursively(String path) throws InterruptedException, KeeperException {
    try {
      delete(path, -1);
    } catch (KeeperException.NotEmptyException e) {
      List<String> children = getChildren(path, null);
      for (String child : children) {
        deleteNodeRecursively(path + "/" + child);
      }
      delete(path, -1);
    } catch (KeeperException.NoNodeException e) {
      // Silently return if the node has already been deleted.
      return;
    }
  }
}
