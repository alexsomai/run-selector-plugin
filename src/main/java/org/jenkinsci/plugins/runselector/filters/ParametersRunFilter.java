/*
 * The MIT License
 *
 * Copyright (c) 2011, Alan Harder
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
package org.jenkinsci.plugins.runselector.filters;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.runselector.RunFilter;
import org.jenkinsci.plugins.runselector.RunFilterDescriptor;
import org.jenkinsci.plugins.runselector.context.RunSelectorContext;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter to find builds matching particular parameters.
 * @author Alan Harder
 */
public class ParametersRunFilter extends RunFilter {
    private final String paramsToMatch;

    private static final Pattern PARAMVAL_PATTERN = Pattern.compile("(.*?)=([^,]*)(,|$)");

    /**
     * @param paramsToMatch comma-separated list of pairs of parameters and values to match
     */
    @DataBoundConstructor
    public ParametersRunFilter(String paramsToMatch) {
        this.paramsToMatch = paramsToMatch;
    }
    
    /**
     * @return comma-separated list of pairs of parameters and values to match
     */
    public String getParamsToMatch() {
        return paramsToMatch;
    }

    private List<StringParameterValue> getFilterParameters(@Nonnull RunSelectorContext context) {
        // Initialize.. parse out the given parameters/values.
        List<StringParameterValue> filters = new ArrayList<StringParameterValue>(5);
        Matcher m = PARAMVAL_PATTERN.matcher(context.getEnvVars().expand(getParamsToMatch()));
        while (m.find()) {
            filters.add(new StringParameterValue(m.group(1), m.group(2)));
        }
        return filters;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable(@Nonnull Run<?,?> run, @Nonnull RunSelectorContext context) {
        EnvVars otherEnv;
        try {
            otherEnv = run.getEnvironment(TaskListener.NULL);
        } catch (Exception ex) {
            return false;
        }
        if(!(run instanceof AbstractBuild)) {
            // Abstract#getEnvironment(TaskListener) put build parameters to
            // environments, but Run#getEnvironment(TaskListener) doesn't.
            // That means we can't retrieve build parameters from WorkflowRun
            // as it is a subclass of Run, not of AbstractBuild.
            // We need expand build parameters manually.
            // See JENKINS-26694 for details.
            for(ParametersAction pa: run.getActions(ParametersAction.class)) {
                // We have to extract parameters manally as ParametersAction#buildEnvVars
                // (overrides EnvironmentContributingAction#buildEnvVars)
                // is applicable only for AbstractBuild.
                for(ParameterValue pv: pa.getParameters()) {
                    pv.buildEnvironment(run, otherEnv);
                }
            }
        }
        List<StringParameterValue> filters = getFilterParameters(context);
        for (StringParameterValue spv : filters) {
            if (!spv.value.equals(otherEnv.get(spv.getName()))) {
                context.logDebug(
                        "{0}: {1} is declined",
                        getDisplayName(),
                        run.getDisplayName()
                );
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getDisplayName() {
        return String.format(
                "%s (%s)",
                super.getDisplayName(),
                getParamsToMatch()
        );
    }

    @Symbol("parameters")
    @Extension
    public static class DescriptorImpl extends RunFilterDescriptor {
        @Override
        public String getDisplayName() {
            return org.jenkinsci.plugins.runselector.Messages.ParametersRunFilter_DisplayName();
        }
    }
}
