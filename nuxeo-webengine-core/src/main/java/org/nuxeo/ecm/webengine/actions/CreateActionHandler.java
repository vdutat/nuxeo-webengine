/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.webengine.actions;


import java.io.IOException;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.webengine.SiteException;
import org.nuxeo.ecm.webengine.SiteObject;
import org.nuxeo.ecm.webengine.SiteRequest;
import org.nuxeo.ecm.webengine.util.DocumentFormHelper;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class CreateActionHandler implements ActionHandler {

    public void run(SiteObject object) throws SiteException {
        if (object.isResolved()) {
            DocumentModel parent = object.getDocument();
            SiteObject child = object.next();
            if (child == null) { /// create a child with a generated name
                DocumentModel doc = createSubPage(parent, null, object.getSiteRequest());
                String path = object.getAbsolutePath();
                if (path.endsWith("/")) {
                    path = path + doc.getName();
                } else {
                    path = path + "/" + doc.getName();
                }
                try {
                    object.getSiteRequest().getResponse().sendRedirect(path);
                } catch (IOException e) {
                    throw new SiteException("Failed to redirect to the newly created page: "+path, e);
                }
                return;
            } else if (!child.isResolved()) {
                String name = child.getName();
                DocumentModel doc = createSubPage(parent, name, object.getSiteRequest());
                child.resolve(doc);
                return;
            }
        }
        throw new SiteException("Faield to create document. The document already exists: "+object.getPath());
    }

    private DocumentModel createSubPage(DocumentModel parent, String name, SiteRequest request)
    throws SiteException {
        try {
            CoreSession session = request.getCoreSession();
            String type = DocumentFormHelper.getDocumentType(request);
            if (type == null) {
                throw new SiteException("Invalid argument exception. Nos doc type specified");
            }
            String path = parent.getPathAsString();
            // TODO  not the best method to create an unnamed doc - should refactor core API
            if (name == null) {
                name = DocumentFormHelper.getDocumentTitle(request);
                if (name == null) {
                    name = IdUtils.generateId(type);
                } else {
                    name = IdUtils.generateId(name);
                }
                String baseTitle = name;
                int i = 0;
                while (true) {
                    try {
                        if (i == 10) throw new SiteException("Failed to create document. Giving up.");
                        session.getDocument(new PathRef(path, name));
                        name = baseTitle+"_"+Long.toHexString(IdUtils.generateLongId());
                        i++;
                    } catch (Exception e) {
                        // the name should be ok
                        break;
                    }
                }
            }
            DocumentModel newPage = session.createDocumentModel(parent.getPathAsString(), name, type);
            DocumentFormHelper.fillDocumentProperties(newPage, request);
            newPage = session.createDocument(newPage);
            session.save();
            return newPage;
        } catch (Exception e) {
            throw new SiteException("Failed to create document: "+name, e);
        }
    }

}