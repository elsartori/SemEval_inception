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
package de.tudarmstadt.ukp.inception.annotation.layer.relation;

import static de.tudarmstadt.ukp.clarin.webanno.api.annotation.util.WebAnnoCasUtil.isBeginEndInSameSentence;
import static de.tudarmstadt.ukp.clarin.webanno.api.annotation.util.WebAnnoCasUtil.isBeginInSameSentence;
import static de.tudarmstadt.ukp.inception.rendering.vmodel.VCommentType.ERROR;
import static java.util.Collections.emptyList;
import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

import de.tudarmstadt.ukp.clarin.webanno.api.annotation.exception.MultipleSentenceCoveredException;
import de.tudarmstadt.ukp.clarin.webanno.api.annotation.util.WebAnnoCasUtil;
import de.tudarmstadt.ukp.clarin.webanno.support.logging.LogMessage;
import de.tudarmstadt.ukp.clarin.webanno.support.uima.ICasUtil;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.inception.rendering.vmodel.VArc;
import de.tudarmstadt.ukp.inception.rendering.vmodel.VComment;
import de.tudarmstadt.ukp.inception.rendering.vmodel.VDocument;
import de.tudarmstadt.ukp.inception.rendering.vmodel.VID;
import de.tudarmstadt.ukp.inception.schema.adapter.AnnotationException;
import de.tudarmstadt.ukp.inception.schema.adapter.TypeAdapter;

/**
 * <p>
 * This class is exposed as a Spring Component via
 * {@code AnnotationServiceAutoConfiguration#relationCrossSentenceBehavior}.
 * </p>
 */
public class RelationCrossSentenceBehavior
    extends RelationLayerBehavior
{
    @Override
    public CreateRelationAnnotationRequest onCreate(RelationAdapter aAdapter,
            CreateRelationAnnotationRequest aRequest)
        throws AnnotationException
    {
        if (aAdapter.getLayer().isCrossSentence()) {
            return aRequest;
        }

        if (!isBeginEndInSameSentence(aRequest.getCas(), aRequest.getOriginFs().getBegin(),
                aRequest.getTargetFs().getEnd())) {
            throw new MultipleSentenceCoveredException("Annotation coveres multiple sentences, "
                    + "limit your annotation to single sentence!");
        }

        return aRequest;
    }

    @Override
    public void onRender(TypeAdapter aAdapter, VDocument aResponse,
            Map<AnnotationFS, VArc> aAnnoToArcIdx)
    {
        if (aAdapter.getLayer().isCrossSentence()) {
            return;
        }

        for (Entry<AnnotationFS, VArc> e : aAnnoToArcIdx.entrySet()) {
            CAS cas = e.getKey().getCAS();

            if (!isBeginInSameSentence(cas,
                    ICasUtil.selectAnnotationByAddr(cas, e.getValue().getSource().getId())
                            .getBegin(),
                    ICasUtil.selectAnnotationByAddr(cas, e.getValue().getTarget().getId())
                            .getBegin())) {
                aResponse.add(new VComment(new VID(e.getKey()), ERROR,
                        "Crossing sentence boundaries is not permitted."));
            }
        }
    }

    @Override
    public List<Pair<LogMessage, AnnotationFS>> onValidate(TypeAdapter aAdapter, CAS aCas)
    {
        // If crossing sentence boundaries is permitted, then there is nothing to validate here
        if (aAdapter.getLayer().isCrossSentence()) {
            return emptyList();
        }

        RelationAdapter adapter = (RelationAdapter) aAdapter;
        Type type = getType(aCas, aAdapter.getAnnotationTypeName());
        Feature targetFeature = type.getFeatureByBaseName(adapter.getTargetFeatureName());
        Feature sourceFeature = type.getFeatureByBaseName(adapter.getSourceFeatureName());

        // If there are no annotations on this layer, nothing to do
        Collection<AnnotationFS> annotations = select(aCas, type);
        if (annotations.isEmpty()) {
            return emptyList();
        }

        // Prepare feedback messsage list
        List<Pair<LogMessage, AnnotationFS>> messages = new ArrayList<>();

        // Build indexes to allow quickly looking up the sentence by its begin/end offsets. Since
        // The indexes are navigable, we can also find the sentences starting/ending closes to a
        // particular offset, even if it is not the start/end offset of a sentence.
        NavigableMap<Integer, AnnotationFS> sentBeginIdx = new TreeMap<>();
        NavigableMap<Integer, AnnotationFS> sentEndIdx = new TreeMap<>();
        for (AnnotationFS sent : select(aCas, getType(aCas, Sentence.class))) {
            sentBeginIdx.put(sent.getBegin(), sent);
            sentEndIdx.put(sent.getEnd(), sent);
        }

        for (AnnotationFS fs : annotations) {
            AnnotationFS sourceFs = (AnnotationFS) fs.getFeatureValue(sourceFeature);
            AnnotationFS targetFs = (AnnotationFS) fs.getFeatureValue(targetFeature);

            Entry<Integer, AnnotationFS> s1 = sentBeginIdx.floorEntry(sourceFs.getBegin());
            Entry<Integer, AnnotationFS> s2 = sentEndIdx.ceilingEntry(targetFs.getEnd());

            if (s1 == null || s2 == null) {
                messages.add(Pair.of(LogMessage.error(this,
                        "Unable to determine any sentences overlapping with [%d-%d]",
                        sourceFs.getBegin(), targetFs.getEnd()), fs));
                continue;
            }

            if (!WebAnnoCasUtil.isSame(s1.getValue(), s2.getValue())) {
                messages.add(Pair.of(
                        LogMessage.error(this, "Crossing sentence boundaries is not permitted."),
                        fs));
            }
        }

        return messages;

    }
}
