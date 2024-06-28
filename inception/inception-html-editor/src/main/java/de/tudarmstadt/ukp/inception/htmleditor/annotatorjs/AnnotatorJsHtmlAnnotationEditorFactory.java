/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.inception.htmleditor.annotatorjs;

import org.apache.wicket.model.IModel;

import de.tudarmstadt.ukp.clarin.webanno.api.CasProvider;
import de.tudarmstadt.ukp.clarin.webanno.api.annotation.paging.NoPagingStrategy;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.inception.editor.AnnotationEditorBase;
import de.tudarmstadt.ukp.inception.editor.AnnotationEditorFactoryImplBase;
import de.tudarmstadt.ukp.inception.editor.action.AnnotationActionHandler;
import de.tudarmstadt.ukp.inception.htmleditor.config.HtmlAnnotationEditorSupportAutoConfiguration;
import de.tudarmstadt.ukp.inception.io.html.HtmlFormatSupport;
import de.tudarmstadt.ukp.inception.io.html.LegacyHtmlFormatSupport;
import de.tudarmstadt.ukp.inception.rendering.editorstate.AnnotatorState;

/**
 * Support for HTML-oriented editor component.
 * <p>
 * This class is exposed as a Spring Component via
 * {@link HtmlAnnotationEditorSupportAutoConfiguration#htmlAnnotationEditorFactory()}.
 * </p>
 */
public class AnnotatorJsHtmlAnnotationEditorFactory
    extends AnnotationEditorFactoryImplBase
{
    @Override
    public String getDisplayName()
    {
        return "HTML (AnnotatorJS)";
    }

    @Override
    public int accepts(Project aProject, String aFormat)
    {
        switch (aFormat) {
        case LegacyHtmlFormatSupport.ID: // fall-through
        case HtmlFormatSupport.ID:
            return PREFERRED;
        default:
            return DEFAULT;
        }
    }

    @Override
    public AnnotationEditorBase create(String aId, IModel<AnnotatorState> aModel,
            AnnotationActionHandler aActionHandler, CasProvider aCasProvider)
    {
        return new AnnotatorJsHtmlAnnotationEditor(aId, aModel, aActionHandler, aCasProvider);
    }

    @Override
    public void initState(AnnotatorState aModelObject)
    {
        aModelObject.setPagingStrategy(new NoPagingStrategy());
    }
}