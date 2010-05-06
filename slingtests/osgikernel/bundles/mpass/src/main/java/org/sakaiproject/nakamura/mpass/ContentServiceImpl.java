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
package org.sakaiproject.nakamura.mpass;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.sakaiproject.nakamura.api.locking.LockManager;
import org.sakaiproject.nakamura.api.locking.LockTimeoutException;
import org.sakaiproject.nakamura.api.mpass.ContentConstants;
import org.sakaiproject.nakamura.api.mpass.MpassException;
import org.sakaiproject.nakamura.api.mpass.ContentService;
import org.sakaiproject.nakamura.api.personal.PersonalUtils;
import org.sakaiproject.nakamura.api.site.SiteException;
import org.sakaiproject.nakamura.api.site.SiteService;
import org.sakaiproject.nakamura.util.JcrUtils;
import org.sakaiproject.nakamura.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

/**
 * Service for doing operations with content.
 */
@Component(immediate = true, label = "Sakai Content Service", description = "Service for doing operations with content.", name = "org.sakaiproject.nakamura.api.mpass.ContentService")
@Service
@Properties(value = { @Property(name = "service.vendor", value = "CRIM") })
public class ContentServiceImpl implements ContentService {

  @Reference
  protected transient LockManager lockManager;

  @Reference
  protected transient SiteService siteService;

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ContentServiceImpl.class);

  
    
  /**
   * 
   * {@inheritDoc}
   * 
   * @throws Exception
   * 
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#create(org.apache.sling.api.resource.Resource)
   */
  public Node create(Session session, Map<String, Object> mapProperties)
      throws MpassException {
    return create(session, mapProperties, null);
  }
  
  private String generateContentId() {
    String contentId = String.valueOf(Thread.currentThread().getId())
        + String.valueOf(System.currentTimeMillis());
    try {
      return org.sakaiproject.nakamura.util.StringUtils.sha1Hash(contentId);
    } catch (Exception ex) {
      throw new MpassException("Unable to create hash.");
    }
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @throws MpassException
   * 
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#create(org.apache.sling.api.resource.Resource)
   */
  public Node create(Session session, Map<String, Object> mapProperties, String contentId)
      throws MpassException {

    if (contentId == null) {
      contentId = generateContentId();
    }

    String user = session.getUserID();
    String contentPathBase = getFullPathToStore(user, session);
    return create(session, mapProperties, contentId, contentPathBase);

  }

  public Node create(Session session, Map<String, Object> mapProperties, String contentId, String contentPathBase)
    throws MpassException {
    Node refl = null;
    try {
      lockManager.waitForLock(contentPathBase);
    } catch (LockTimeoutException e1) {
      throw new MpassException("Unable to lock user content");
    }
    try {
      String contentPath = PathUtils.toSimpleShardPath(contentPathBase, contentId, "");
      try {
        refl = JcrUtils.deepGetOrCreateNode(session, contentPath);
        
        for (Entry<String, Object> e : mapProperties.entrySet()) {
          String val = e.getValue().toString();
          try {
            long l = Long.parseLong(val);
            refl.setProperty(e.getKey(), l);
          } catch (NumberFormatException ex) {
            refl.setProperty(e.getKey(), val);
          }
        }
        // Add the id for this content.
        refl.setProperty(ContentConstants.PROP_SAKAI_ID, contentId);
        Calendar cal = Calendar.getInstance();
        refl.setProperty(ContentConstants.PROP_SAKAI_CREATED, cal);
        refl.setProperty(ContentConstants.PROP_SAKAI_MODIFIED, cal);

        if (session.hasPendingChanges()) {
          session.save();
        }
        
      } catch (RepositoryException e) {
        LOGGER.warn("RepositoryException on trying to save content."
            + e.getMessage());
        throw new MpassException("Unable to save content.");
      }
      return refl;
    } finally {
      lockManager.clearLocks();
    }
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#getContentStorePathFromContentNode(javax.jcr.Node)
   */
  public String getContentStorePathFromContentNode(Node refl)
      throws ValueFormatException, PathNotFoundException,
      ItemNotFoundException, AccessDeniedException, RepositoryException {
    Node n = refl;
    while (!"/".equals(n.getPath())) {
      if (n.hasProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY)
          && ContentConstants.SAKAI_CONTENTSTORE_RT.equals(n.getProperty(
              JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY).getString())) {
        return n.getPath();
      }
      n = n.getParent();
    }
    return null;
  }

  /**
   * 
   * {@inheritDoc}
   * @throws RepositoryException 
   * @throws PathNotFoundException 
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#copyContentNode(java.lang.String, java.lang.String, java.lang.String)
   */
  public void copyContentNode(Node sourceContent, String targetStore) throws PathNotFoundException, RepositoryException {
    Session session = sourceContent.getSession();
    String contentId = sourceContent.getName();
    String targetNodePath = PathUtils.toSimpleShardPath(targetStore, contentId, "");
    String parent = targetNodePath.substring(0, targetNodePath.lastIndexOf('/'));
    Node parentNode = JcrUtils.deepGetOrCreateNode(session, parent);
    LOGGER.info("Created parent node at: " + parentNode.getPath());
    session.save();
    session.getWorkspace().copy(sourceContent.getPath(), targetNodePath);
  }


  /**
   * 
   * {@inheritDoc}
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#isContentStore(javax.jcr.Node)
   */
  public boolean isContentStore(Node n) {
    try {
      if (n.hasProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY)
          && ContentConstants.SAKAI_CONTENTSTORE_RT.equals(n.getProperty(
              JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY).getString())) {
        return true;
      }
    } catch (RepositoryException e) {
      return false;
    }

    return false;
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#getFullPathToContent(java.lang.String,
   *      java.lang.String)
   */
  public String getFullPathToContent(String rcpt, String contentId, Session session) throws MpassException {
    String storePath = getFullPathToStore(rcpt, session);
    return PathUtils.toSimpleShardPath(storePath, contentId, "");
  }

  /**
   * 
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.mpass.ContentService#getFullPathToStore(java.lang.String)
   */
  public String getFullPathToStore(String rcpt, Session session) throws MpassException {
    String path = "";
    try {
      if (rcpt.startsWith("s-")) {
        // This is a site.
        Node n = siteService.findSiteByName(session, rcpt.substring(2));
        path = n.getPath() + "/store";
      } else {
        Authorizable au = PersonalUtils.getAuthorizable(session, rcpt);
        path = PersonalUtils.getHomeFolder(au) + "/" + ContentConstants.FOLDER_CONTENT;
      }
    } catch (SiteException e) {
      LOGGER.warn("Caught SiteException when trying to get the full path to {} store.", rcpt,e);
      throw new MpassException(e.getStatusCode(), e.getMessage());
    } catch (RepositoryException e) {
      LOGGER.warn("Caught RepositoryException when trying to get the full path to {} store.", rcpt,e);
      throw new MpassException(500, e.getMessage());
    }

    return path;
  }

}
