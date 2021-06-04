/*
 * Copyright 2021 HyperDevs
 *
 * Copyright 2020 BQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mini

import org.amshove.kluent.`should be equal to`
import org.junit.Test
import java.io.Closeable

class CompositeCloseableTest {

    @Test
    fun itemsAreClosed() {
        val c = CompositeCloseable()
        val dummyCloseable = DummyCloseable()
        c.add(dummyCloseable)
        c.close()
        c.close()

        dummyCloseable.closed.`should be equal to`(1)
    }

    class DummyCloseable : Closeable {
        var closed = 0
        override fun close() {
            closed++
        }
    }
}