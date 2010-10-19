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
package org.sakaiproject.nakamura.api.mpass;

/**
 *
 */
public interface ContentConstants {

  /**
   *
   */
  public static final String SAKAI_CONTENTSTORE_RT = "sakai/contentstore";
  
  /**
  *
  */
  public static final String SAKAI_CONTENT_RT = "sakai/content";

  /**
   * This property will hold the value for the id of this message.
   */
  public static final String PROP_SAKAI_ID = "sakai:id";

  /**
   * The value for this property will define what kind of content this is. ex: internal,
   * email, ..
   */
  public static final String PROP_SAKAI_TYPE = "sakai:type";
  
  /**
   * This property will hold the value for whom this content was created.
   */
  public static final String PROP_SAKAI_FROM = "sakai:from";
  
  /**
   * This property will hold the value for the title.
   */
  public static final String PROP_SAKAI_SUBJECT = "sakai:title";
  
  /**
   * This property will hold the value for the body.
   */
  public static final String PROP_SAKAI_BODY = "sakai:body";

  /**
   * Value for a date.
   */
  public static final String PROP_SAKAI_CREATED = "sakai:created";
  
  /**
   * Value for a date.
   */
  public static final String PROP_SAKAI_MODIFIED = "sakai:modified";
  
  /**
   * If a post has been deleted.
   */
  public static final String PROP_DELETED = "sakai:deleted";
  
  /**
   * Holds the value of people who edited this message.
   */
  public static final String PROP_EDITEDBY = "sakai:editedby";
  
  /**
   * Holds the profile info for the editors.
   */
  public static final String PROP_EDITEDBYPROFILES = "sakai:editedbyprofiles";
  
  /**
   * JCR folder name for content.
   */
  public static final String FOLDER_CONTENT = "content";

  /**
   * Identifier for a reflection.
   */
  public static final String TYPE_REFLECTION = "reflection";

  /**
   * Identifier for an assignment.
   */
  public static final String TYPE_ASSIGNMENT = "assignment";

  /**
   * The user content path.
   */
  public static final String SEARCH_PROP_CONTENTSTORE = "_userContentPath";
  public static final String SEARCH_PROP_MESSAGEROOT = "_contentStoreRoot";

  public static final String PROP_SAKAI_MESSAGEERROR = "sakai:messageError";

  public static final String PROP_SAKAI_CONTENT_TYPE = "sakai:contentType";

  public static final String PROP_SAKAI_ATTACHMENT_DESCRIPTION = "sakai:attachmentDescription";

  public static final String PROP_SAKAI_ATTACHMENT_CONTENT = "sakai:attachmentContent";

  /**
   * Values for the preprocessor
   */
  public static final String REG_PROCESSOR_NAMES = "sakai.content.createpreprocessor";
  public static final String CONTENT_CREATE_PREPROCESSOR = "CreateContentPreProcessor";

  public static final int CLEAUNUP_EVERY_X_SECONDS = 7200;
}
