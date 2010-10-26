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
package com.fraudwall.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link SQLResultsClosure} that executes a SQL statement and collects the
 * results from each row into a List. The
 * {@link #getElementFromResult(AnchorResultSet, int)} method must be overridden
 * to determine what information to extract from each database row in the
 * query's result set.<p>
 *
 * The generic parameter <code>T</code> is the type of data extracted from
 * each row of the result set. The result of the {@link #exec(AnchorResultSet)}
 * method is a list of T.<p>
 *
 * @see SQLResultsClosure
 * @see SQLResultsProcessorClosure
 * @see SQLIgnoreResultsClosure
 */
public abstract class SQLResultsListClosure<T> extends SQLResultsClosure<List<T>> {

	/**
	 * Constructs a new closure that runs the given <code>sql</code> statement
	 * and returns a list of the objects returned from
	 * {@link #getElementFromResult}.
	 */
	public SQLResultsListClosure(String sql) {
		super(sql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> exec(AnchorResultSet rs) throws SQLException {
		List<T> list = (List<T>) new ArrayList<Object>();
		int rowNum = 0;
		while (rs.next()) {
			list.add(getElementFromResult(rs, rowNum));
		}
		return list;
	}

	protected abstract T getElementFromResult(AnchorResultSet rs, int rowNum) throws SQLException;

}
