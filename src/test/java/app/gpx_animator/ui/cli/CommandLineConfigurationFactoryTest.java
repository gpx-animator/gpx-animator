/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.ui.cli;

import app.gpx_animator.core.Option;
import app.gpx_animator.core.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineConfigurationFactoryTest {

    public static final String TEST_COLOR_FF_0096 = "#FF0096";
    public static final String TEST_FONT_MONOSPACED_8 = "Monospaced 8";

    @ParameterizedTest
    @EnumSource(Option.class)
    void checkInputParamsByOptionParams(final Option option) throws UserException {
        if (option.equals(Option.GUI)) {
            return;
        }
        final var op = OptionParam.ofOption(option);
        final var supplierParam = op.getSupplierParam();
        assertNotNull(supplierParam);
        final var optArgument = supplierParam.get();
        final var args = new ArrayList<>();
        args.add("--".concat(option.getName()));
        optArgument.ifPresent(args::add);
        if (op.isNeedTrackConfiguration()) {
            args.add("--".concat(OptionParam.INPUT.getOption().getName()));
            OptionParam.INPUT.getSupplierParam().get().ifPresent(args::add);
        }
        CommandLineConfigurationFactory factory = new CommandLineConfigurationFactory(args.toArray(String[]::new));
        assertTrue(op.getCheckResult().apply(factory));
    }

    @Test
    void testOutputWhenInputIsSet() throws UserException {
        final var args = new String[] {
                "--input", "journey.gpx",
                "--input", "anotherJourney.gpx",
        };

        CommandLineConfigurationFactory factory = new CommandLineConfigurationFactory(args);
        assertNotNull(factory.getConfiguration().getOutput());
        assertEquals(factory.getConfiguration().getOutput().getName(), "journey.mp4");
    }

    @Test
    void testMultipleInputParams() throws UserException {
        // given --input with multiple input files
        final var args = new String[]{"--input", "input1.gpx", "input2.gpx", "input3.gpx", "--output", "dummyOutput.mp4"};

        // when creating the configuration
        var factory = new CommandLineConfigurationFactory(args);

        // then the configuration contains all input files
        assertEquals(3, factory.getConfiguration().getTrackConfigurationList().size());
        assertEquals("input1.gpx", factory.getConfiguration().getTrackConfigurationList().get(0).getInputGpx().getName());
        assertEquals("input2.gpx", factory.getConfiguration().getTrackConfigurationList().get(1).getInputGpx().getName());
        assertEquals("input3.gpx", factory.getConfiguration().getTrackConfigurationList().get(2).getInputGpx().getName());
    }
}
