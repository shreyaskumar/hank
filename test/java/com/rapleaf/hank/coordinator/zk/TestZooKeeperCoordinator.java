/**
 *  Copyright 2011 Rapleaf
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.rapleaf.hank.coordinator.zk;

import com.rapleaf.hank.ZkTestCase;
import com.rapleaf.hank.coordinator.DomainChangeListener;
import com.rapleaf.hank.coordinator.DomainConfig;
import com.rapleaf.hank.coordinator.DomainGroupChangeListener;
import com.rapleaf.hank.coordinator.DomainGroupConfig;
import com.rapleaf.hank.coordinator.HostConfig;
import com.rapleaf.hank.coordinator.PartDaemonState;
import com.rapleaf.hank.coordinator.RingGroupChangeListener;
import com.rapleaf.hank.coordinator.RingGroupConfig;
import com.rapleaf.hank.coordinator.HostConfig.HostStateChangeListener;


public class TestZooKeeperCoordinator extends ZkTestCase {

  public class OmniMockListener implements RingGroupChangeListener, DomainChangeListener, DomainGroupChangeListener, HostStateChangeListener {
    public RingGroupConfig ringGroup;
    public boolean notified = false;

    @Override
    public void onRingGroupChange(RingGroupConfig newRingGroup) {
      this.ringGroup = newRingGroup;
      notified = true;
      synchronized (this) {
        notifyAll();
      }
    }

    public DomainGroupConfig domainGroup;

    @Override
    public void onDomainGroupChange(DomainGroupConfig newDomainGroup) {
      this.domainGroup = newDomainGroup;
      notified = true;
      synchronized (this) {
        notifyAll();
      }
    }

    public DomainConfig newDomain;

    @Override
    public void onDomainChange(DomainConfig newDomain) {
      this.newDomain = newDomain;
      notified = true;
      synchronized (this) {
        notifyAll();
      }
    }

    public HostConfig hostConfig;

    @Override
    public void onHostStateChange(HostConfig hostConfig) {
      this.hostConfig = hostConfig;
      notified = true;
      synchronized (this) {
        notifyAll();
      }
    }
  }

  private final String domains_root = getRoot() + "/domains";
  private final String domain_groups_root = getRoot() + "/domain_groups";
  private final String ring_groups_root = getRoot() + "/ring_groups";
  private ZooKeeperCoordinator coord;

  public TestZooKeeperCoordinator() throws Exception {
    super();
  }

  public void testLoad() throws Exception {
    // check standard loading stuff
    assertEquals("number of loaded domain configs", 1, coord.getDomainConfigs().size());
    assertEquals("get domain by name", "domain0", coord.getDomainConfig("domain0").getName());

    assertEquals("number of loaded domain group configs", 1, coord.getDomainGroupConfigs().size());
    assertEquals("get domain group by name", "myDomainGroup", coord.getDomainGroupConfig("myDomainGroup").getName());

    assertEquals("number of loaded ring groups", 1, coord.getRingGroups().size());
    assertEquals("get ring group by name", "myRingGroup", coord.getRingGroupConfig("myRingGroup").getName());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    create(domains_root);
    createMockDomain(domains_root + "/domain0");
    create(domain_groups_root);
    create(domain_groups_root + "/myDomainGroup");
    create(domain_groups_root + "/myDomainGroup/domains");
    create(domain_groups_root + "/myDomainGroup/versions");
    create(ring_groups_root);
    create(ring_groups_root + "/myRingGroup", domain_groups_root + "/myDomainGroup");
    create(ring_groups_root + "/myRingGroup/ring-001");
    create(ring_groups_root + "/myRingGroup/ring-001/version", "1");
    create(ring_groups_root + "/myRingGroup/ring-001/hosts");
    create(ring_groups_root + "/myRingGroup/ring-001/hosts/localhost:1");
    create(ring_groups_root + "/myRingGroup/ring-001/hosts/localhost:1/part_daemon");
    create(ring_groups_root + "/myRingGroup/ring-001/hosts/localhost:1/part_daemon/status", PartDaemonState.STARTABLE.name());
    create(ring_groups_root + "/myRingGroup/ring-001/hosts/localhost:1/parts");

    coord = getCoord();
  }

  private ZooKeeperCoordinator getCoord() throws Exception {
    return new ZooKeeperCoordinator(getZkConnectString(), 100000000, domains_root, domain_groups_root, ring_groups_root);
  }
}
