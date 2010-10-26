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
/*
 * Copyright (c) 2007, Fraudwall Technologies. All rights reserved.
 */
package com.fraudwall.util.progress;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A Java port of Martyn J. Pearce's perl Term::ProgressBar - provide a
 * progress meter on a standard terminal.<p>
 *
 * ProgressBar provides a simple progress bar on the terminal, to let
 * the user know that something is happening, roughly how much stuff has
 * been done, and maybe an estimate at how long remains.<p>
 *
 * A typical use sets up the progress bar with a number of items to do,
 * and then calls update to update the bar whenever an item is
 * processed.<p>
 *
 * Often, this would involve updating the progress bar many times with
 * no user-visible change. To avoid unnecessary work, the update method
 * returns a value, being the update value at which the user will next
 * see a change. By only calling update when the current value exceeds
 * the next update value, the call overhead is reduced.<p>
 *
 * Remember to call the "progress.update(max_value)" when the job is
 * done to get a nice 100% done bar.<p>
 *
 * A progress bar by default is simple; it just goes from left-to-right,
 * filling the bar with '=' characters. These are called major
 * characters. For long-running jobs, this may be too slow, so two
 * additional features are available: a linear completion time
 * estimator, and/or a minor character: this is a character that *moves*
 * from left-to-right on the progress bar (it does not fill it as the
 * major character does), traversing once for each major-character
 * added. This exponentially increases the granularity of the bar for
 * the same width.
 *
 * @see FileInputStreamProgressBar
 * @see CountingProgressBar
 * @author Michael Radwin
 */
/*package*/ class ProgressBar {
	private long offset, last_update, final_target;
	private int term_width, bar_width;
	private boolean minor, pb_ended, remove;
	private boolean ETA;
	private double max_update_rate, scale;
	private long start;
	private PrintStream fh;
	private String name;

	private static final String LBRACK = "[";
	private static final String RBRACK = "]";
	private static final char MINOR_CHAR = '*';
	private static final char MAJOR_CHAR = '=';

	private static final int MINUTE = 60;
	private static final int HOUR   = 60 * MINUTE;
	private static final int DAY    = 24 * HOUR;

	// The point past which to give ETA of just date, rather than time
	private static final int ETA_DATE_CUTOFF = 3 * DAY;
	// The point past which to give ETA of time, rather time left
	private static final int ETA_TIME_CUTOFF = 10 * MINUTE;
	// The ratio prior to which to not dare any estimates
	private static final double PREDICT_RATIO = 0.01;

	private DateFormat dfHourMin, dfDayMonth, dfHourAmPm;

	public ProgressBar(long target) {
		this(target, false, null, 80);
	}

	public ProgressBar(long target, boolean ETA) {
		this(target, ETA, null, 80);
	}

	public ProgressBar(long target, boolean ETA, String name) {
		this(target, ETA, name, 80);
	}

	public ProgressBar(long target, boolean ETA, String name, int term_width) {
		this.last_update = 0;
		this.offset = 0;
		this.scale = 1.0;
		this.term_width = term_width;

		setName(name);
		setOutput(System.err);
		setMaxUpdateRate(0.5);
		setRemove(false);
		setETA(ETA);

		setBarWidth();
		setFinalTarget(target);
		setMinor(target > Math.pow(bar_width, 1.5));

		this.start = System.currentTimeMillis() / 1000;
		update(0);
	}

	protected void setBarWidth() {
		bar_width = term_width - 5; // 5 for the % marker
		bar_width -= LBRACK.length();
		bar_width -= RBRACK.length();
		if (name != null) {
			bar_width -= name.length();
			bar_width -= 2;
		}
		if (ETA) {
			bar_width -= 10;
		}
		if (bar_width < 1) {
			System.err.println("terminal width "
							   + term_width
							   + "too small for bar; defaulting to 10");
			bar_width = 10;
		}
	}

	/**
	 * The filehandle to output to. Defaults to {@link System#err}.
	 */
	public void setOutput(PrintStream fh) {
		this.fh = fh;
	}

	public PrintStream getOutput() {
		return fh;
	}

	/**
	 * Default: set. If unset, no minor scale will be calculated or
	 * updated.<p>
	 *
	 * Minor characters are used on the progress bar to give the user
	 * the idea of progress even when there are so many more tasks than
	 * the terminal is wide that the granularity would be too great. By
	 * default, ProgressBar makes a guess as to when minor characters
	 * would be valuable. However, it may not always guess right, so
	 * this method may be called to force it one way or the other. Of
	 * course, the efficiency saving is minimal unless the client is
	 * utilizing the return value of update.
	 */
	public void setMinor(boolean minor) {
		this.minor = minor;
	}

	public boolean getMinor() {
		return minor;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public boolean getRemove() {
		return remove;
	}

	/**
	 * This value is taken as being the maximum speed between updates to
	 * aim for.  It is only meaningful if ETA is switched on. It
	 * defaults to 0.5, being the number of seconds between updates.
	 */
	public void setMaxUpdateRate(double maxUpdateRate) {
		this.max_update_rate = maxUpdateRate;
	}

	public double getMaxUpdateRate() {
		return max_update_rate;
	}

	/**
	 * A total time estimation to use. If enabled, a time finished
	 * estimation is printed on the RHS (once sufficient updates have
	 * been performed to make such an estimation feasible). Naturally,
	 * this is an *estimate*; no guarantees are made. The format of the
	 * estimate
	 *
	 * Note that the format is intended to be as compact as possible
	 * while giving over the relevant information. Depending upon the
	 * time remaining, the format is selected to provide some resolution
	 * whilst remaining compact. Since the time remaining decreases, the
	 * format typically changes over time.
	 *
	 * As the ETA approaches, the format will state minutes & seconds
	 * left. This is identifiable by the word 'Left' at the RHS of the
	 * line. If the ETA is further away, then an estimate time of
	 * completion (rather than time left) is given, and is identifiable
	 * by 'ETA' at the LHS of the ETA box (on the right of the progress
	 * bar). A time or date may be presented; these are of the form of a
	 * 24 hour clock, e.g. '13:33', a time plus days (e.g., ' 7PM+3' for
	 * around in over 3 days time) or a day/date, e.g. ' 1Jan' or
	 * '27Feb'.
	 *
	 * If ETA is switched on, the return value of update is also
	 * affected: the idea here is that if the progress bar seems to be
	 * moving quicker than the eye would normally care for (and thus a
	 * great deal of time is spent doing progress updates rather than
	 * "real" work), the next value is increased to slow it. The maximum
	 * rate aimed for is tunable via the max_update_rate component.
	 */
	public void setETA(boolean ETA) {
		this.ETA = ETA;
		if (this.ETA && dfHourMin == null) {
			dfHourMin = new SimpleDateFormat("HH:mm");
			dfDayMonth = new SimpleDateFormat("dMMM");
			dfHourAmPm = new SimpleDateFormat("ha");
		}
		setBarWidth();
	}

	public boolean getETA() {
		return ETA;
	}

	/**
	 * A name to prefix the progress bar with.
	 */
	public void setName(String name) {
		this.name = name;
		setBarWidth();
	}

	public String getName() {
		return name;
	}

	/**
	 * The final target.  Updates are measured in terms of this.
	 * Changes will have no effect until the next update, but the next
	 * update value should be relative to the new target.  So
	 *
	 *  ProgressBar pb = new ProgressBar(20);
	 *  // Halfway
	 *  pb.update(10);
	 *  // Double scale
	 *  pb.setTarget(40);
	 *  pb.update(21);
	 *
	 * will cause the progress bar to update to 52.5%
	 */
	public void setFinalTarget(long target) {
		minor = term_width < target;
		this.final_target = target;
	}

	public long getFinalTarget() {
		return final_target;
	}

	/**
	 * Output a message.  This is very much like print, but we try not
	 * to disturb the terminal.
	 */
	public void message(String message) {
		fh.print('\r');
		for (int i = 0; i < term_width; i++) {
			fh.print(' ');
		}
		fh.print('\r');
		fh.println(message);
		update(last_update);
	}

	/**
	 * Update the progress bar.
	 */
	public long update(long iso_far) {
		long input_so_far = iso_far;
		double so_far = iso_far;
		if (scale != 1.0) {
			so_far *= scale;
		}

		so_far += offset;

		double target, next;
		target = next = this.final_target;
		if (target < 1) {
			fh.print('\r');
			if (name != null) {
				fh.print(name);
				fh.print(": ");
			}
			fh.print("(nothing to do)\n");
			return Integer.MAX_VALUE;
		}

		double major_units = bar_width / target;
		double biggies = major_units * so_far;
		int ibiggies = (int) biggies;
		char chars[] = new char[bar_width];
		for (int i = 0; i < bar_width; i++) {
			chars[i] = ' ';
		}
		for (int i = 0; i < ibiggies; i++) {
			chars[i] = MAJOR_CHAR;
		}

		if (minor) {
			double minor_units = (bar_width * bar_width) / target;
			if (so_far != target) {
				double smally = minor_units * so_far % bar_width;
				chars[(int) smally] = MINOR_CHAR;
			}
			next *= (minor_units * so_far + 1.0) /
				(bar_width * bar_width);
		} else {
			next *= (major_units * so_far + 1.0) /
				bar_width;
		}

		StringBuilder buf = new StringBuilder();
		buf.append('\r');
		if (name != null) {
			buf.append(name).append(": ");
		}
		double ratio = so_far / target;

		// Rounds down %
		int percent = (int) (ratio * 100.0);
		if (percent > 99) {
			buf.append((char)('0' + ((percent / 100)%10)));
		} else {
			buf.append(' ');
		}
		if (percent > 9) {
			buf.append((char)('0' + ((percent / 10)%10)));
		} else {
			buf.append(' ');
		}
		buf.append((char)('0' + (percent)%10));
		buf.append("% ").append(LBRACK).append(chars).append(RBRACK);

		if (ETA) {
			if (ratio == 1.0) {
				long taken = (System.currentTimeMillis() / 1000) - start;
				long ss = taken % 60;
				long mm = (taken % 3600) / 60;
				long hh = taken / 3600;
				if (hh > 99) {
					buf.append("D ");
					buf.append((char)('0' + ((hh / 100)%10)));
					buf.append((char)('0' + ((hh / 10)%10)));
					buf.append((char)('0' + (hh)%10));
					buf.append('h');
					buf.append((char)('0'+((mm/10)%10)));
					buf.append((char)('0'+(mm%10)));
					buf.append('m');
				} else {
					buf.append("D");
					if (hh > 9) {
						buf.append((char)('0' + ((hh / 10)%10)));
					} else {
						buf.append(' ');
					}
					buf.append((char)('0' + (hh)%10));
					buf.append('h');
					buf.append((char)('0'+((mm/10)%10)));
					buf.append((char)('0'+(mm%10)));
					buf.append('m');
					buf.append((char)('0'+((ss/10)%10)));
					buf.append((char)('0'+(ss%10)));
					buf.append('s');
				}
			} else if (ratio < PREDICT_RATIO) {
				// No safe prediction yet
				buf.append("ETA ------");
			} else {
				long time = System.currentTimeMillis();
				double left = ((time/1000 - start) * ((1.0 - ratio) / ratio));
				if (left < ETA_TIME_CUTOFF) {
					int mm = (int)(left / 60.0);
					int ss = ((int)left) % 60;
					buf.append((char)('0'+(mm%10)));
					buf.append('m');
					buf.append((char)('0'+((ss/10)%10)));
					buf.append((char)('0'+(ss%10)));
					buf.append("s Left");
				} else {
					Date date = new Date(time + (long)(left * 1000.0));
					if (left < DAY) {
						buf.append("ETA  ").append(dfHourMin.format(date));
					} else if (left < ETA_DATE_CUTOFF) {
						buf.append("ETA ").append(dfHourAmPm.format(date))
							.append('+').append((int)(left / DAY));
					} else {
						buf.append("ETA ").append(dfDayMonth.format(date));
					}
				}

				// Calculate next to be at least SEC_PER_UPDATE seconds away
				if (left > 0.0) {
					double incr = (target - so_far) / (left / max_update_rate);
					if (so_far + incr > next) {
						next = so_far + incr;
					}
				}
			}
		}

		fh.print(buf);

		next -= offset;
		if (scale != 1.0) {
			next /= scale;
		}

		if (so_far >= target && remove && !pb_ended) {
			fh.print('\r');
			for (int i = 0; i < term_width; i++) {
				fh.print(' ');
			}
			fh.print('\r');
			pb_ended = true;
		}

		long inext = (long)next;
		if (inext > this.final_target) {
			inext = this.final_target;
		}

		last_update = input_so_far;
		return inext;
	}

	public long update() {
		return update(last_update + 1);
	}
}

/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */
