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

package tachyon.util.io;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class ByteUtilsTest {
  @Test
  public void cloneByteBufferTest() {
    final int bufferSize = 10;
    ByteBuffer buf = ByteBuffer.allocate(bufferSize);
    ByteBuffer bufClone = null;
    for (byte i = 0; i < bufferSize; i ++) {
      buf.put(i);
    }
    bufClone = BufferUtils.cloneByteBuffer(buf);
    Assert.assertTrue(bufClone.equals(buf));
  }

  @Test
  public void cloneDirectByteBufferTest() {
    final int bufferSize = 10;
    ByteBuffer bufDirect = ByteBuffer.allocateDirect(bufferSize);
    ByteBuffer bufClone = null;
    for (byte i = 0; i < bufferSize; i ++) {
      bufDirect.put(i);
    }
    bufClone = BufferUtils.cloneByteBuffer(bufDirect);
    Assert.assertTrue(bufClone.equals(bufDirect));
  }
}
