/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.mpass;

/**
 * General exception for MPASS content.
 */
public class MpassException extends RuntimeException {
  /**
   * Default serial version UID
   */
  private static final long serialVersionUID = 1L;

  private int code;
  private String message;
  
  
  public MpassException() {
    super();
  }

  public MpassException(int code) {
    super();
    setCode(code);
  }  
  public MpassException(int code, String message) {
    super();
    setCode(code);
    setMessage(message);
  }



  public MpassException(String message, Throwable cause) {
    super(message, cause);
    setMessage(message);
  }

  public MpassException(String message) {
    super(message);
    setMessage(message);
  }

  public MpassException(Throwable cause) {
    super(cause);
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
