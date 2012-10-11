/*
 * Copyright 2011 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax

enum TapeMode {

	READ_WRITE(true, true, false),
	READ_ONLY(true, false, false),
	READ_SEQUENTIAL(true, false, true),
	WRITE_ONLY(false, true, false),
	WRITE_SEQUENTIAL(false, true, true),
	DEFAULT(false, false, false)

	final boolean readable
	final boolean writable
	final boolean sequential

	private TapeMode(boolean readable, boolean writable, boolean sequential) {
		this.readable = readable
		this.writable = writable
		this.sequential = sequential
	}

	boolean asBoolean() {
		readable || writable
	}

}