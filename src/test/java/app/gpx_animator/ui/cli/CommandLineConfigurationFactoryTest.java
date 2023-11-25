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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineConfigurationFactoryTest {

    @ParameterizedTest
    @EnumSource(Option.class)
    void checkInputParamsByOptionParams(final Option option) throws UserException {
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

}
