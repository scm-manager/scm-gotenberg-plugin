/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, { FC, useEffect } from "react";
import { useConfigLink } from "@scm-manager/ui-api";
import { HalRepresentation } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import { Checkbox, ConfigurationForm, InputField, Title, validation } from "@scm-manager/ui-components";

type Props = {
  link: string;
};

type Configuration = HalRepresentation & {
  url: string;
  enabled: boolean;
};

const GotenbergConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const { initialConfiguration, isReadOnly, update, ...formProps } = useConfigLink<Configuration>(link);
  const { formState, handleSubmit, register, reset } = useForm<Configuration>({ mode: "onChange" });
  const {errors} = formState;

  useEffect(() => {
    if (initialConfiguration) {
      // TODO check why the type error
      reset(initialConfiguration);
    }
  }, [initialConfiguration]);

  return (
    <ConfigurationForm
      isValid={formState.isValid}
      isReadOnly={isReadOnly}
      onSubmit={handleSubmit(update)}
      {...formProps}
    >
      <Title title={t("scm-gotenberg-plugin.config.title")} />
      <InputField
        label={t("scm-gotenberg-plugin.config.url")}
        helpText={t("scm-gotenberg-plugin.config.urlHelpText")}
        disabled={isReadOnly}
        errorMessage={t("scm-gotenberg-plugin.config.invalid")}
        validationError={!!errors.url}
        {...register("url", {
          validate: url => validation.isUrlValid(url)
        })}
      />
      <Checkbox
        label={t("scm-gotenberg-plugin.config.enabled")}
        helpText={t("scm-gotenberg-plugin.config.enabledHelpText")}
        disabled={isReadOnly}
        {...register("enabled")}
      />
    </ConfigurationForm>
  );
};

export default GotenbergConfiguration;
