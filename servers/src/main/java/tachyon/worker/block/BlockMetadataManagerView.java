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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import tachyon.exception.NotFoundException;
import tachyon.master.BlockInfo;
import tachyon.worker.block.meta.BlockMeta;
import tachyon.worker.block.meta.StorageTier;
import tachyon.worker.block.meta.StorageTierView;

/**
 * This class exposes a narrower view of {@link BlockMetadataManager} to Evictors and Allocators,
 * filtering out un-evictable blocks and un-allocatable space (TODO) internally, so that evictors
 * and allocators can be developed with much simpler logic, without worrying about various
 * constraints, e.g. pinned files, locked blocks, etc.
 */
public class BlockMetadataManagerView {

  /** The BlockMetadataManager this view is derived from */
  private final BlockMetadataManager mMetadataManager;
  /** A list of StorageTierView, derived from StorageTiers from the BlockMetadataManager */
  private List<StorageTierView> mTierViews = new ArrayList<StorageTierView>();
  /** A list of pinned inodes */
  private final Set<Integer> mPinnedInodes = new HashSet<Integer>();
  /** A list of blocks that are currently being read */
  private final Set<Long> mLockedBlocks = new HashSet<Long>();
  /** A map from tier alias to StorageTierView */
  private Map<Integer, StorageTierView> mAliasToTierViews = new HashMap<Integer, StorageTierView>();


  /**
   * Constructor of BlockMatadataManagerView. Now we always creating a new view before freespace.
   * TODO: incrementally update the view
   *
   * @param manager which the view should be constructed from
   * @param pinnedInodes, a set of pinned nodes
   * @param lockedBlocks, a set of locked blocks
   * @return BlockMetadataManagerView constructed
   */
  public BlockMetadataManagerView(BlockMetadataManager manager, Set<Integer> pinnedInodes,
      Set<Long> lockedBlocks) {
    mMetadataManager = Preconditions.checkNotNull(manager);
    mPinnedInodes.addAll(Preconditions.checkNotNull(pinnedInodes));
    mLockedBlocks.addAll(Preconditions.checkNotNull(lockedBlocks));

    // iteratively create all StorageTierViews and StorageDirViews
    for (StorageTier tier : manager.getTiers()) {
      StorageTierView tierView = new StorageTierView(tier, this);
      mTierViews.add(tierView);
      mAliasToTierViews.put(tier.getTierAlias(), tierView);
    }
  }

  /**
   * Test if the block is pinned.
   *
   * @param blockId to be tested
   * @return boolean, true if block is pinned
   */
  public boolean isBlockPinned(long blockId) {
    return mPinnedInodes.contains(BlockInfo.computeInodeId(blockId));
  }

  /**
   * Test if the block is locked.
   *
   * @param blockId to be tested
   * @return boolean, true if block is locked
   */
  public boolean isBlockLocked(long blockId) {
    return mLockedBlocks.contains(blockId);
  }

  /**
   * Test if the block is evictable
   *
   * @param blockId to be tested
   * @return boolean, true if the block can be eveicted
   */
  public boolean isBlockEvictable(long blockId) {
    return (!isBlockPinned(blockId) && !isBlockLocked(blockId));
  }

  /**
   * Provide StorageTierView given tierAlias
   *
   * @param tierAlias the alias of this tierView
   * @return the StorageTierView object associated with the alias
   * @throws IllegalArgumentException if tierAlias is not found
   */
  public StorageTierView getTierView(int tierAlias) {
    StorageTierView tierView = mAliasToTierViews.get(tierAlias);
    if (null == tierView) {
      throw new IllegalArgumentException("Cannot find tier view with alias: " + tierAlias);
    } else {
      return tierView;
    }
  }

  /**
   * Get all tierViews under this managerView
   *
   * @return the list of StorageTierViews
   */
  public List<StorageTierView> getTierViews() {
    return mTierViews;
  }

  /**
   * Get all tierViews before certain tierView
   *
   * @param tierAlias the alias of a tierView
   * @return the list of StorageTierView
   * @throws IllegalArgumentException if tierAlias is not found
   */
  public List<StorageTierView> getTierViewsBelow(int tierAlias) {
    int level = getTierView(tierAlias).getTierViewLevel();
    return mTierViews.subList(level + 1, mTierViews.size());
  }

  /**
   * Get available bytes given certain location Redirecting to
   * {@link BlockMetadataManager#getAvailableBytes(BlockStoreLocation)}
   *
   * @param location location the check available bytes
   * @return available bytes
   * @throws IllegalArgumentException if location does not belong to tiered storage
   */
  public long getAvailableBytes(BlockStoreLocation location) {
    return mMetadataManager.getAvailableBytes(location);
  }

  /**
   * Return null if block is pinned or currently being locked, otherwise return
   * {@link BlockMetadataManager#getBlockMeta(long)}
   *
   * @param blockId the block ID
   * @return metadata of the block or null
   * @throws NotFoundException if no BlockMeta for this blockId is found
   */
  public BlockMeta getBlockMeta(long blockId) throws NotFoundException {
    if (isBlockEvictable(blockId)) {
      return mMetadataManager.getBlockMeta(blockId);
    } else {
      return null;
    }
  }
}
