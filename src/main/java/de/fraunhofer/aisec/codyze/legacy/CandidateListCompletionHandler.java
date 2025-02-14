/*
 * Copyright (c) 2002-2012, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * This class is a c/p backport of JLine 2.14.6 (commit a27f3bdd6df899224a3dc9d9f3a6511c6230c0b3).
 *
 * It removes the trailing space after tab completions.
 */

package de.fraunhofer.aisec.codyze.legacy;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.jline.console.ConsoleReader;
import org.python.jline.console.CursorBuffer;
import org.python.jline.console.completer.CompletionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link CompletionHandler} that deals with multiple distinct completions
 * by outputting the complete list of possibilities to the console. This
 * mimics the behavior of the
 * <a href="http://www.gnu.org/directory/readline.html">readline</a> library.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public class CandidateListCompletionHandler implements CompletionHandler {

	@Override
	public boolean complete(final ConsoleReader reader, final List<CharSequence> candidates, final int pos) throws IOException {
		CursorBuffer buf = reader.getCursorBuffer();

		// if there is only one completion, then fill in the buffer
		if (candidates.size() == 1) {
			CharSequence value = candidates.get(0);

			// fail if the only candidate is the same as the current buffer
			if (value.equals(buf.toString())) {
				return false;
			}

			setBuffer(reader, value, pos);

			return true;
		} else if (candidates.size() > 1) {
			String value = getUnambiguousCompletions(candidates);
			setBuffer(reader, value, pos);
		}

		printCandidates(reader, candidates);

		// redraw the current console buffer
		reader.drawLine();

		return true;
	}

	public static void setBuffer(final ConsoleReader reader, final CharSequence value, final int offset) throws IOException {
		//noinspection StatementWithEmptyBody
		while ((reader.getCursorBuffer().cursor > offset) && reader.backspace()) {
			// empty
		}

		reader.putString(value);
		reader.setCursorPosition(offset + value.length());
	}

	/**
	 * Print out the candidates. If the size of the candidates is greater than the
	 * {@link ConsoleReader#getAutoprintThreshold}, they prompt with a warning.
	 *
	 * @param candidates the list of candidates to print
	 */
	public static void printCandidates(final ConsoleReader reader, Collection<CharSequence> candidates) throws IOException {
		Set<CharSequence> distinct = new HashSet<>(candidates);

		if (distinct.size() > reader.getAutoprintThreshold()) {
			//noinspection StringConcatenation
			reader.println();
			reader.print(Messages.DISPLAY_CANDIDATES.format(candidates.size()));
			reader.flush();

			int c;

			String noOpt = Messages.DISPLAY_CANDIDATES_NO.format();
			String yesOpt = Messages.DISPLAY_CANDIDATES_YES.format();
			char[] allowed = { yesOpt.toLowerCase().charAt(0), yesOpt.toUpperCase().charAt(0), noOpt.toLowerCase().charAt(0), noOpt.toUpperCase().charAt(0) };

			while ((c = reader.readCharacter(allowed)) != -1) {
				String tmp = new String(new char[] { (char) c });

				if (noOpt.toUpperCase().startsWith(tmp.toUpperCase())) {
					reader.println();
					return;
				} else if (yesOpt.toUpperCase().startsWith(tmp.toUpperCase())) {
					break;
				} else {
					reader.beep();
				}
			}
		}

		// copy the values and make them distinct, without otherwise affecting the ordering. Only do it if the sizes differ.
		if (distinct.size() != candidates.size()) {
			Collection<CharSequence> copy = new ArrayList<>();

			for (CharSequence next : candidates) {
				if (!copy.contains(next)) {
					copy.add(next);
				}
			}

			candidates = copy;
		}

		reader.println();
		reader.printColumns(candidates);
	}

	/**
	 * Returns a root that matches all the {@link String} elements of the specified {@link List},
	 * or null if there are no commonalities. For example, if the list contains
	 * <i>foobar</i>, <i>foobaz</i>, <i>foobuz</i>, the method will return <i>foob</i>.
	 */
	private @NonNull String getUnambiguousCompletions(@NonNull final List<CharSequence> candidates) {
		if (candidates.isEmpty()) {
			return "";
		}

		// convert to an array for speed
		CharSequence[] strings = candidates.toArray(new CharSequence[0]);

		CharSequence first = strings[0];
		int end = first.length();

		for (int i = 1; i < strings.length || end == 0; i++) {
			CharSequence next = strings[i];
			end = Math.min(end, next.length());

			for (int j = 0; j < end; j++) {
				if (first.charAt(j) != next.charAt(j)) {
					end = j;
				}
			}
		}
		return first.subSequence(0, end).toString();
	}
}

enum Messages {
	DISPLAY_CANDIDATES("Display all methods? (Y/N)"),
	DISPLAY_CANDIDATES_YES("y"),
	DISPLAY_CANDIDATES_NO("n"),
	;

	String msg;
	Messages(String msg) {
		this.msg = msg;
	}

	public String format(final Object... args) {
		return String.format(this.msg, args);
	}
}
