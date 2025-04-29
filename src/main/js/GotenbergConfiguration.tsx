/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import { useConfigLink } from "@scm-manager/ui-api";
import { Checkbox, ConfigurationForm, InputField, Title, validation } from "@scm-manager/ui-components";
import { HalRepresentation } from "@scm-manager/ui-types";

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
