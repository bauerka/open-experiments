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

import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.sakaiproject.nakamura.api.doc.BindingType;
import org.sakaiproject.nakamura.api.doc.ServiceBinding;
import org.sakaiproject.nakamura.api.doc.ServiceDocumentation;
import org.sakaiproject.nakamura.api.doc.ServiceMethod;
import org.sakaiproject.nakamura.api.doc.ServiceParameter;
import org.sakaiproject.nakamura.api.doc.ServiceResponse;
import org.sakaiproject.nakamura.api.doc.ServiceSelector;
import org.sakaiproject.nakamura.api.mpass.ContentConstants;
import org.sakaiproject.nakamura.api.user.UserConstants;
import org.sakaiproject.nakamura.util.ExtendedJSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Will create a content in the user's contentstore (/_user/s/si/simong/content... folder.
 */
@SlingServlet(resourceTypes = { "sakai/contentstore" }, selectors = { "create" }, generateComponent = true, methods = { "POST" })
@Properties(value = {
    @Property(name = "service.vendor", value = "CRIM"),
    @Property(name = "service.description", value = "Endpoint to create a content") })
@ServiceDocumentation(
    name = "CreateContentServlet",
    shortDescription = "Create a content.",
    description = "Create a content by doing a POST to content.create.html . By default there are stores under each site and at /_user/u/us/user/content and /_group/g/gr/group/content",
    bindings = @ServiceBinding(type = BindingType.TYPE, 
        bindings = "sakai/content", 
        selectors = @ServiceSelector(name = "create",description="Create a new content for MPASS")), 
    methods = @ServiceMethod(name = "POST",
        description = "Create a content. <br />" +
        "This servlet will only create content nodes. " + 
        "All other POST headers sent along in this request will end up as properties on the content-node. <br />" +
        "Example:<br />" +
        "curl -d\"sakai:to=internal:user1\" -d\"sakai:title=Title\" -d\"sakai:body=Loremlipsum\" http://user2:test2@localhost:8080/_user/content.create.html",
        response = {
          @ServiceResponse(code = 200, description = "The servlet will send a JSON response which holds 2 keys." + 
            "<ul><li>id: The id for the newly created content.</li><li>content: This is an object which will hold all the key/values for the newly created content.</li></ul>"),
          @ServiceResponse(code = 400, description = "The request did not contain all the (correct) parameters."),
          @ServiceResponse(code = 401, description = "The user is not logged."),
          @ServiceResponse(code = 500, description = "The server was unable to create the content.")},
        parameters = {
          @ServiceParameter(name = "sakai:title", description = "Title of content")}))
public class CreateContentServlet extends AbstractContentServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1641554633533952863L;
  
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CreateContentServlet.class);

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.mpass.AbstractContentServlet#handleOperation(org.apache.sling.api.SlingHttpServletRequest,
   *      org.apache.sling.api.servlets.HtmlResponse, java.util.List)
   */
  @Override
  protected void doPost(SlingHttpServletRequest request,
      org.apache.sling.api.SlingHttpServletResponse response)
      throws javax.servlet.ServletException, java.io.IOException {

    // This is the content store resource.
    Resource baseResource = request.getResource();
    Session session = request.getResourceResolver().adaptTo(Session.class);
    
    // Current user.
    String user = request.getRemoteUser();

    // Anonymous users are not allowed to add anything.
    if (user == null || UserConstants.ANON_USERID.equals(request.getRemoteUser())) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "Anonymous users can't add content.");
      return;
    }

    RequestParameterMap mapRequest = request.getRequestParameterMap();
    Map<String, Object> mapProperties = new HashMap<String, Object>();

    for (Entry<String, RequestParameter[]> e : mapRequest.entrySet()) {
      RequestParameter[] parameter = e.getValue();
      if (parameter.length == 1) {
        mapProperties.put(e.getKey(), parameter[0].getString());
      }
      else {
        String[] arr = new String[parameter.length];
        for (int i = 0;i<parameter.length;i++) {
          arr[i] = parameter[i].getString();
        }
        mapProperties.put(e.getKey(), arr);
      }
    }
    mapProperties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
        ContentConstants.SAKAI_CONTENT_RT);
    mapProperties.put(ContentConstants.PROP_SAKAI_FROM, user);

    // Create the content.
    Node content = null;
    String path = null;
    String contentId = null;
    try {
      content = getContentService().create(session, mapProperties);
      if (content == null) {
        throw new Exception("Unable to create the content");
      }
      path = content.getPath();
      contentId = content.getProperty(ContentConstants.PROP_SAKAI_ID).getString();

      LOGGER.info("Got content node as " + content);
    } catch (RepositoryException e) {
      LOGGER.warn("RepositoryException: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    } catch (Exception e) {
      LOGGER.warn("Exception: " + e.getMessage());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    }

    baseResource.getResourceMetadata().setResolutionPath("/");
    baseResource.getResourceMetadata().setResolutionPathInfo(path);

    final String finalPath = path;
    final ResourceMetadata rm = baseResource.getResourceMetadata();

    // Wrap the request so it points to the content we just created.
    ResourceWrapper wrapper = new ResourceWrapper(request.getResource()) {
      /**
       * {@inheritDoc}
       * 
       * @see org.apache.sling.api.resource.ResourceWrapper#getPath()
       */
      @Override
      public String getPath() {
        return finalPath;
      }

      /**
       * {@inheritDoc}
       * 
       * @see org.apache.sling.api.resource.ResourceWrapper#getResourceType()
       */
      @Override
      public String getResourceType() {
        return "sling/servlet/default";
      }

      /**
       * {@inheritDoc}
       * 
       * @see org.apache.sling.api.resource.ResourceWrapper#getResourceMetadata()
       */
      @Override
      public ResourceMetadata getResourceMetadata() {
        return rm;
      }

    };

    RequestDispatcherOptions options = new RequestDispatcherOptions();
    SlingHttpServletResponseWrapper wrappedResponse = new SlingHttpServletResponseWrapper(
        response) {
      ServletOutputStream servletOutputStream = new ServletOutputStream() {

        @Override
        public void write(int b) throws IOException {
        }
      };
      PrintWriter pw = new PrintWriter(servletOutputStream);

      /**
       * {@inheritDoc}
       * 
       * @see javax.servlet.ServletResponseWrapper#flushBuffer()
       */
      @Override
      public void flushBuffer() throws IOException {
      }

      /**
       * {@inheritDoc}
       * 
       * @see javax.servlet.ServletResponseWrapper#getOutputStream()
       */
      @Override
      public ServletOutputStream getOutputStream() throws IOException {
        return servletOutputStream;
      }

      /**
       * {@inheritDoc}
       * 
       * @see javax.servlet.ServletResponseWrapper#getWriter()
       */
      @Override
      public PrintWriter getWriter() throws IOException {
        return pw;
      }
    };
    options.setReplaceSelectors("");
    LOGGER.info("Sending the request out again.");
    request.getRequestDispatcher(wrapper, options).forward(request, wrappedResponse);
    response.reset();
    try {
      Node contentNode = (Node) session.getItem(path);

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      JSONWriter write = new JSONWriter(response.getWriter());
      write.object();
      write.key("id");
      write.value(contentId);
      write.key("content");
      ExtendedJSONWriter.writeNodeToWriter(write, contentNode);
      write.endObject();
    } catch (JSONException e) {
      throw new ServletException(e.getMessage(), e);
    } catch (RepositoryException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }
}