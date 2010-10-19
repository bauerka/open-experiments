/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.mpass.servlet;

import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.sakaiproject.nakamura.api.mpass.ContentService;

/**
 * 
 */
public class AbstractContentServlet extends SlingAllMethodsServlet {

  /**
   * @scr.reference name="contentService"
   */
  private ContentService contentService;
  /**
   * 
   */
  private static final long serialVersionUID = -3530686442766919201L;

  /**
   * 
   */
  public AbstractContentServlet() {
  }

  /**
   * @param contentService
   */
  protected void bindContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * @param contentService
   */
  protected void unbindContentService(ContentService contentService) {
    this.contentService = null;
  }
  
  /**
   * @return the contentService
   */
  public ContentService getContentService() {
    return contentService;
  }

}
