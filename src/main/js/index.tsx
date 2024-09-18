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

import { binder } from "@scm-manager/ui-extensions";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import ConvertedPdfViewer from "./ConvertedPdfViewer";
import { File } from "@scm-manager/ui-types";
import GotenbergConfiguration from "./GotenbergConfiguration";

type FileProps = {
  file?: File;
};

const pdfLinkPredicate = (props: FileProps) => {
  return !!props.file?._links?.pdf;
};

binder.bind("repos.sources.view", ConvertedPdfViewer, pdfLinkPredicate);

cfgBinder.bindGlobal(
  "/gotenberg",
  "scm-gotenberg-plugin.config.link",
  "gotenbergConfig",
  GotenbergConfiguration
);
