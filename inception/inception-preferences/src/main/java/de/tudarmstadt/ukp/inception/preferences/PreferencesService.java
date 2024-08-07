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
package de.tudarmstadt.ukp.inception.preferences;

import java.util.List;

import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.clarin.webanno.security.model.User;
import de.tudarmstadt.ukp.inception.preferences.model.DefaultProjectPreference;
import de.tudarmstadt.ukp.inception.preferences.model.UserProjectPreference;

public interface PreferencesService
{
    <T> T loadTraitsForUser(Key<T> aKey, User aUser);

    <T> void saveTraitsForUser(Key<T> aKey, User aUser, T aTraits);

    <T> T loadTraitsForUserAndProject(Key<T> aKey, User aUser, Project aProject);

    <T> void saveTraitsForUserAndProject(Key<T> aKey, User aUser, Project aProject, T aTraits);

    List<UserProjectPreference> listUserPreferencesForProject(Project aProject);

    void saveUserProjectPreference(UserProjectPreference aPreference);

    <T> T loadDefaultTraitsForProject(Key<T> aKey, Project aProject);

    <T> void saveDefaultTraitsForProject(Key<T> aKey, Project aProject, T aTraits);

    <T> void clearDefaultTraitsForProject(Key<T> aKey, Project aProject);

    List<DefaultProjectPreference> listDefaultTraitsForProject(Project aProject);

    void saveDefaultProjectPreference(DefaultProjectPreference aPreference);
}
