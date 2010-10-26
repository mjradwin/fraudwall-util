/**
 * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of Anchor Intelligence nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.fraudwall.util.db;

import java.sql.SQLException;


/**
 * A {@link SQLResultsClosure} that executes a SQL statement and invokes a method
 * on each row of the result set. The
 * {@link #processResult(AnchorResultSet, int)} method must be overridden;
 * this method gets called on each result set row.
 *
 * @see SQLResultsClosure
 * @see SQLResultsListClosure
 * @see SQLIgnoreResultsClosure
 */
public abstract class SQLResultsProcessorClosure extends SQLResultsClosure<Object> {
	/**
	 * Constructs a new {@link SQLResultsProcessorClosure} that runs the query
	 * specified by the given {@code sql} text.
	 */
	public SQLResultsProcessorClosure(String sql) {
		super(sql);
	}

	@Override
	public Void exec(AnchorResultSet results) throws SQLException {
		int count = 0;
		while (results.next()) {
			processResult(results, count++);
		}
		return null;
	}

	/**
	 * Method invoked on each row of the query's result set. Prior to this method being
	 * called, the {@link AnchorResultSet#next()} method will have been called on
	 * {@code results} and returned a result of {@code true}.
	 *
	 * @param rowNum The 0-based row number of the given <code>results</code>.
	 */
	protected abstract void processResult(AnchorResultSet results, int rowNum) throws SQLException;
}
