/*
 * Checkstyle Beans: A NetBeans checkstyle integration plugin.
 * Copyright (C) 2007-1013  Petr Hejl
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package cz.sickboy.netbeans.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Context;
import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;
import cz.sickboy.netbeans.checkstyle.editor.CheckstyleTask;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * The checkstyle checker that calls for the cancel status of the
 * {@link CheckstyleTask} and cancel itself if the task is cancelled.
 *
 * @author Petr Hejl
 * @see CheckstyleTask
 */
public class CancellableChecker extends Checker {

    private static final TreeSet<LocalizedMessage> EMPTY_SET = new TreeSet<LocalizedMessage>() {

        @Override
        public boolean add(LocalizedMessage e) {
            throw new UnsupportedOperationException("Read only set");
        }

        @Override
        public boolean addAll(Collection<? extends LocalizedMessage> c) {
            throw new UnsupportedOperationException("Read only set");
        }
    };

    private final CancellationHook hook;

    /**
     * Contructs the checker that won't do any checks whenewer the task has cancelled
     * status set to <code>true</code>.
     *
     * @param task the task that will be consulted for the cancellation
     * @throws CheckstyleException if any problem with initialization occurs
     */
    public CancellableChecker(CancellationHook hook) throws CheckstyleException {
        this.hook = hook;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addFileSetCheck(FileSetCheck fileSetCheck) {
        super.addFileSetCheck(new CancellableFileSetCheck(fileSetCheck, hook));
    }

    /**
     * Checks the given file.
     *
     * @param file file to check
     * @see Checker#process(File[])
     */
    public void process(File file) {
        process(Collections.singletonList(file));
    }

    public static interface CancellationHook {

        boolean isCanceled();

    }

    private static class CancellableFileSetCheck implements FileSetCheck {

        private final FileSetCheck check;

        private final CancellationHook hook;

        public CancellableFileSetCheck(FileSetCheck check, CancellationHook hook) {
            this.check = check;
            this.hook = hook;
        }

        public void contextualize(Context context) throws CheckstyleException {
            check.contextualize(context);
        }

        public void configure(Configuration configuration) throws CheckstyleException {
            check.configure(configuration);
        }

        public void setMessageDispatcher(MessageDispatcher dispatcher) {
            check.setMessageDispatcher(dispatcher);
        }

        public TreeSet<LocalizedMessage> process(File file, List<String> lines) {
            if (hook.isCanceled()) {
                return EMPTY_SET;
            }
            return check.process(file, lines);
        }

        public void init() {
            check.init();
        }

        public void finishProcessing() {
            check.finishProcessing();
        }

        public void destroy() {
            check.destroy();
        }

        public void beginProcessing(String charset) {
            check.beginProcessing(charset);
        }
     }
}
