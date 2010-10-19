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

import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

/**
 * Manages the content of a user.
 */
public interface ContentService {
  
  /**
   * Creates a new content for the user associated with the provided session. Content
   * properties are extracted from the supplied map. The contentId supplied must be
   * guaranteed unique
   * 
   * @param resource
   * @param mapProperties
   * @param contentId
   *          Globally unique content identifier
   * @return
   * @throws MpassException
   */
  public Node create(Session session, Map<String, Object> mapProperties, String contentId)
      throws MpassException;

  /**
   * Creates a new content for the user associated with the provided session. Content
   * properties are extracted from the supplied map
   * 
   * @param resource
   * @param mapProperties
   * @return
   * @throws MpassException
   */
  public Node create(Session session, Map<String, Object> mapProperties)
      throws MpassException;
  
  /**
   * @param session
   * @param mapProperties
   * @param contentId
   * @param contentPathBase
   * @return
   * @throws MpassException
   */
  Node create(Session session, Map<String, Object> mapProperties, String contentId,
      String contentPathBase) throws MpassException;
  
  /**
   * Gets the absolute path to the content store from a content. ex:
   * /_private/D0/33/E2/admin/mpass/content
   * 
   * @param refl
   *          A content node
   * @return
   */
  public String getContentStorePathFromContentNode(Node rfl) throws ValueFormatException,
      PathNotFoundException, ItemNotFoundException, AccessDeniedException,
      RepositoryException;

  /**
   * Gets the full JCR path for a given recipient and a content ID.
   * @param rcpt The recipient. Can be either a site, group or a user. Sites should be prefixed with s-, groups with g-.
   * @param contentId The ID of the content.
   * @param session
   * @return The JCR path to that content.
   * @throws MpassException
   */
  public String getFullPathToContent(String rcpt, String contentId, Session session) throws MpassException;

  /**
   * Gets the full JCR path to a store for a recipient.
   * @param rcpt The recipient. Can be either a site, group or a user. Sites should be prefixed with s-, groups with g-.
   * @param session
   * @return The JCR path to the store.
   * @throws MpassException
   */
  public String getFullPathToStore(String rcpt, Session session) throws MpassException;

  /**
   * Copies a content with id <em>contentId</em> from <em>source</em> to <em>target</em>
   * 
   * @param adminSession
   * @param sourceContent
   * @param targetContentStore
   * @throws RepositoryException
   * @throws PathNotFoundException
   */
  public void copyContentNode(Node sourceContent, String targetContentStore) throws PathNotFoundException, RepositoryException;

  /**
   * Checks if the provided node is a content store node.
   * 
   * @param n
   * @return
   */
  public boolean isContentStore(Node n);
}
