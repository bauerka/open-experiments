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

import static org.sakaiproject.nakamura.api.user.UserConstants.SYSTEM_USER_MANAGER_USER_PATH;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.apache.sling.servlets.post.Modification;
import org.sakaiproject.nakamura.api.mpass.ContentConstants;
import org.sakaiproject.nakamura.api.personal.PersonalUtils;
import org.sakaiproject.nakamura.api.user.UserConstants;
import org.sakaiproject.nakamura.api.user.UserPostProcessor;
import org.sakaiproject.nakamura.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

/**
 * This PostProcessor listens to post operations on User objects and creates a content
 * store. 
 */
@Component(immediate = true, label = "ContentUserPostProcessor", description = "Creates the content stores for users and groups.", metatype = false)
@Service
@Properties(value = {
    @Property(name = "service.vendor", value = "CRIM"),
    @Property(name = "service.description", value = "Creates the content stores for users and groups.") })
public class ContentUserPostProcessor implements UserPostProcessor {

  
  private static final Logger LOGGER = LoggerFactory.getLogger(ContentUserPostProcessor.class);
  
  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.user.UserPostProcessor#getSequence()
   */
  public int getSequence() {
    return 10;
  }

  
  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.user.UserPostProcessor#process(org.apache.jackrabbit.api.security.user.Authorizable,
   *      javax.jcr.Session, org.apache.sling.api.SlingHttpServletRequest, java.util.List)
   */
  public void process(Authorizable authorizable, Session session,
      SlingHttpServletRequest request, List<Modification> changes) throws Exception {

    String resourcePath = request.getRequestPathInfo().getResourcePath();
    if (resourcePath.equals(SYSTEM_USER_MANAGER_USER_PATH)) {

      String path = PersonalUtils.getHomeFolder(authorizable) + "/"
          + ContentConstants.FOLDER_CONTENT;
      LOGGER.debug("Getting/creating content store: {}", path);

      Node contentStore = JcrUtils.deepGetOrCreateNode(session, path);
      contentStore.setProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
          ContentConstants.SAKAI_CONTENTSTORE_RT);
      
      PrincipalManager principalManager = AccessControlUtil.getPrincipalManager(session);
      Principal anonymous = new Principal() {
        public String getName() {
          return UserConstants.ANON_USERID;
        }
      };
      String[] privileges = new String[] { Privilege.JCR_READ, Privilege.JCR_WRITE };
      
      AccessControlUtil.replaceAccessControlEntry(session, contentStore.getPath(), 
          authorizable.getPrincipal(), privileges, null, null);
      
      // explicitly deny anonymous and everyone, this is private space
      AccessControlUtil.replaceAccessControlEntry(session, contentStore.getPath(), 
          principalManager.getEveryone(), null, privileges, null);
      AccessControlUtil.replaceAccessControlEntry(session, contentStore.getPath(), 
          anonymous, null, privileges, null);
      
      if (session.hasPendingChanges()) {
        session.save();
      }
    }
  }

}
