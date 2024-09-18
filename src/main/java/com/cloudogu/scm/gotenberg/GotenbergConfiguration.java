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

package com.cloudogu.scm.gotenberg;

import com.cloudogu.conveyor.GenerateDto;
import com.cloudogu.conveyor.Include;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@Data
@GenerateDto
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GotenbergConfiguration {

  @URL
  @Include
  private String url = "http://localhost:3000";

  @Include
  private boolean enabled = false;

}
