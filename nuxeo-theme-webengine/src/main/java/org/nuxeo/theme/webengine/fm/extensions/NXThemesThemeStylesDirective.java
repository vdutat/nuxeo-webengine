/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Jean-Marc Orliaguet, Chalmers
 *
 * $Id$
 */

package org.nuxeo.theme.webengine.fm.extensions;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nuxeo.ecm.webengine.WebEngine;
import org.nuxeo.ecm.webengine.model.WebContext;
import org.nuxeo.theme.html.ui.ThemeStyles;
import org.nuxeo.theme.themes.ThemeManager;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author <a href="mailto:jmo@chalmers.se">Jean-Marc Orliaguet</a>
 * 
 */
public class NXThemesThemeStylesDirective implements TemplateDirectiveModel {

    public void execute(Environment env, Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body) throws TemplateException, IOException {

        if (loopVars.length != 0) {
            throw new TemplateModelException(
                    "This directive doesn't allow loop variables.");
        }
        if (body != null) {
            throw new TemplateModelException("Didn't expect a body");
        }

        Writer writer = env.getOut();

        WebContext context = WebEngine.getActiveContext();
        HttpServletRequest request = context.getRequest();

        final URL themeUrl = (URL) request.getAttribute("org.nuxeo.theme.url");

        String themeName;
        if (params.containsKey("theme")) {
            themeName = params.get("theme").toString();
        } else {
            themeName = ThemeManager.getThemeNameByUrl(themeUrl);
        }

        String collectionName = ThemeManager.getCollectionNameByUrl(themeUrl);

        Boolean cache = false;
        if (params.containsKey("cache")) {
            cache = Boolean.parseBoolean(params.get("cache").toString());
        }

        Boolean inline = false;
        if (params.containsKey("inline")) {
            inline = Boolean.parseBoolean(params.get("inline").toString());
        }

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("themeName", themeName);
        attributes.put("path", context.getModulePath());
        attributes.put("basepath", context.getBasePath());
        attributes.put("collection", collectionName);

        Boolean virtualHosting = Utils.isWebEngineVirtualHosting(request);
        writer.write(ThemeStyles.render(attributes, cache, inline,
                virtualHosting));
    }
}
