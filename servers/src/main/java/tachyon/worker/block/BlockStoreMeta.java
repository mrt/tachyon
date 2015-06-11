/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.worker.block;

import com.google.common.base.Preconditions;
import tachyon.worker.block.meta.StorageDir;
import tachyon.worker.block.meta.StorageTier;

import java.util.*;

/**
 * This class holds the meta data information of a block store.
 * <p>
 * TODO: use proto buf to represent this information
 */
public class BlockStoreMeta {
  List<Long> mCapacityBytesOnTiers = new ArrayList<Long>();
  List<Long> mUsedBytesOnTiers = new ArrayList<Long>();
  Map<Long, List<Long>> mBlockIdsOnDirs = new HashMap<Long, List<Long>>();

  public BlockStoreMeta(BlockMetadataManager manager) {
    Preconditions.checkNotNull(manager);
    for (StorageTier tier : manager.getTiers()) {
      mCapacityBytesOnTiers.add(tier.getCapacityBytes());
      mUsedBytesOnTiers.add(tier.getCapacityBytes() - tier.getAvailableBytes());
      for (StorageDir dir : tier.getStorageDirs()) {
        mBlockIdsOnDirs.put(dir.getStorageDirId(), dir.getBlockIds());
      }
    }
  }

  public List<Long> getCapacityBytesOnTiers() {
    return mCapacityBytesOnTiers;
  }

  public List<Long> getUsedBytesOnTiers() {
    return mUsedBytesOnTiers;
  }

  public Map<Long, List<Long>> getBlockList() {
    return mBlockIdsOnDirs;
  }

}
