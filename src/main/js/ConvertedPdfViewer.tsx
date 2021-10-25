import React, {FC} from "react";
import { File } from "@scm-manager/ui-types";
import { PdfViewer } from "@scm-manager/ui-components";

type Props = {
  file: File;
};

const ConvertedPdfViewer:FC<Props> = ({file}) => (
  <PdfViewer src={file._links.pdf.href} />
);

export default ConvertedPdfViewer;
