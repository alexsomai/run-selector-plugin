/*
 * The MIT License
 * 
 * Copyright (c) 2015 IKEDA Yasuyuki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.runselector.selectors;

import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.runselector.RunSelector;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * {@link RunSelector} enumerates only one build.
 * override {{@link #getBuild(Job, RunSelectorContext)} instead.
 */
public abstract class AbstractSpecificRunSelector extends RunSelector {
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public final Run<?, ?> getNextBuild(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context) throws IOException, InterruptedException {
        if (context.getLastMatchBuild() != null) {
            return null;
        }
        return getBuild(job, context);
    }
    
    /**
     * Override this method to implement {@link AbstractSpecificRunSelector}.
     * 
     * @param job       the job to pick a build from.
     * @param context   context for the current execution of runselector.
     * @return the build to select
     * @throws IOException if an error occurs while performing the operation.
     * @throws InterruptedException if any thread interrupts the current thread.
     */
    @CheckForNull
    public abstract Run<?, ?> getBuild(@Nonnull Job<?, ?> job, @Nonnull RunSelectorContext context) throws IOException, InterruptedException;
}
