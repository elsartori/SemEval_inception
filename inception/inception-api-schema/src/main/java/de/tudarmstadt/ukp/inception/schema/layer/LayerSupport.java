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
package de.tudarmstadt.ukp.inception.schema.layer;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.springframework.beans.factory.BeanNameAware;

import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationFeature;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationLayer;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.inception.rendering.Renderer;
import de.tudarmstadt.ukp.inception.schema.AnnotationSchemaService;
import de.tudarmstadt.ukp.inception.schema.adapter.TypeAdapter;

public interface LayerSupport<A extends TypeAdapter, T>
    extends BeanNameAware
{
    String getId();

    /**
     * Checks whether the given layer is provided by the current layer support.
     * 
     * @param aLayer
     *            a layer definition.
     * @return whether the given layer is provided by the current layer support.
     */
    boolean accepts(AnnotationLayer aLayer);

    /**
     * Get the layer type for the given annotation layer. If the current layer support does not
     * provide any layer type for the given layer, an empty value is returned. As we usually use
     * {@link LayerType} objects in layer type selection lists, this method is helpful in obtaining
     * the selected value of such a list from the {@link AnnotationLayer} object being edited.
     * 
     * @param aLayer
     *            a layer definition.
     * @return the corresponding layer type.
     */
    default Optional<LayerType> getLayerType(AnnotationLayer aLayer)
    {
        return getSupportedLayerTypes().stream().filter(t -> t.getName().equals(aLayer.getType()))
                .findFirst();
    }

    List<LayerType> getSupportedLayerTypes();

    /**
     * Create an adapter for the given annotation layer.
     * 
     * @param aLayer
     *            the annotation layer.
     * @param aFeatures
     *            the features of that layer (all features)
     * @return the adapter.
     */
    A createAdapter(AnnotationLayer aLayer, Supplier<Collection<AnnotationFeature>> aFeatures);

    /**
     * Add the types required for this layer to the given type system.
     * 
     * @param aTsd
     *            the type system description to which to add the generated types and features.
     * @param aLayer
     *            the layer for which to generate the types and features (one layer can cause
     *            multiple types to be generated, each layer can have multiple features).
     * @param aAllFeaturesInProject
     *            a list of all features defined in the project. Providing this list here avoids
     *            having to fetch the features for every layer which can lead to database hammering.
     *            Just call {@link AnnotationSchemaService#listAnnotationFeature(Project)} once
     *            before generating types for one or more layers.
     */
    void generateTypes(TypeSystemDescription aTsd, AnnotationLayer aLayer,
            List<AnnotationFeature> aAllFeaturesInProject);

    /**
     * @param aLayer
     *            a layer
     * @return the names of the UIMA types which are generated by the given layer.
     */
    default List<String> getGeneratedTypeNames(AnnotationLayer aLayer)
    {
        return asList(aLayer.getName());
    }

    Renderer createRenderer(AnnotationLayer aLayer,
            Supplier<Collection<AnnotationFeature>> aFeatures);

    /**
     * Returns a Wicket component to configure the specific traits of this layer type. Note that
     * every {@link LayerSupport} has to return a <b>different class</b> here. So it is not possible
     * to simple return a Wicket {@link Panel} here, but it must be a subclass of {@link Panel} used
     * exclusively by the current {@link LayerSupport}. If this is not done, then the traits editor
     * in the UI will not be correctly updated when switching between layer types!
     * 
     * @param aId
     *            a markup ID.
     * @param aLayerModel
     *            a model holding the annotation layer for which the traits editor should be
     *            created.
     * @return the traits editor component .
     */
    default Panel createTraitsEditor(String aId, IModel<AnnotationLayer> aLayerModel)
    {
        return new EmptyPanel(aId);
    }

    default T createTraits()
    {
        return null;
    }

    /**
     * Read the traits for the given {@link AnnotationLayer}. If traits are supported, then this
     * method must be overwritten. A typical implementation would read the traits from a JSON string
     * stored in {@link AnnotationLayer#getTraits}, but it would also possible to load the traits
     * from a database table.
     * 
     * @param aLayer
     *            the layer whose traits should be obtained.
     * @return the traits.
     */
    default T readTraits(AnnotationLayer aLayer)
    {
        return null;
    }

    /**
     * Write the traits for the given {@link AnnotationLayer}. If traits are supported, then this
     * method must be overwritten. A typical implementation would write the traits from to JSON
     * string stored in {@link AnnotationLayer#setTraits}, but it would also possible to store the
     * traits from a database table.
     * 
     * @param aLayer
     *            the layer whose traits should be written.
     * @param aTraits
     *            the traits.
     */
    default void writeTraits(AnnotationLayer aLayer, T aTraits)
    {
        aLayer.setTraits(null);
    }

    default IllegalArgumentException unsupportedLayerTypeException(AnnotationLayer aLayer)
    {
        return new IllegalArgumentException(
                "Unsupported type [" + aLayer.getType() + "] on layer [" + aLayer.getName() + "]");
    }

    void setLayerSupportRegistry(LayerSupportRegistry aLayerSupportRegistry);

    LayerSupportRegistry getLayerSupportRegistry();
}
