package com.rallydev.intellij.wsapi.cache;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.rallydev.intellij.wsapi.domain.AttributeDefinition;
import com.rallydev.intellij.wsapi.domain.TypeDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(
        name = "Rally Type Definition Cache",
        storages = @Storage(file = "$APP_CONFIG$/rally_type_definition_cache.xml")
)
public class TypeDefinitionCache implements PersistentStateComponent<TypeDefinitionCache> {

    //com.intellij.util.xmlb.XmlSerializer fails to serializes a map of maps, inner classes to deal with this
    public Map<String, TypeDefinitionEntry> typeDefinitionsByWorkspace = new HashMap<String, TypeDefinitionEntry>();
    public Map<String, AttributeDefinitionEntry> attributeDefinitionsByWorkspace = new HashMap<String, AttributeDefinitionEntry>();

    @Nullable
    @Override
    public TypeDefinitionCache getState() {
        return this;
    }

    @Override
    public void loadState(TypeDefinitionCache state) {
        typeDefinitionsByWorkspace = new HashMap<String, TypeDefinitionEntry>();
        attributeDefinitionsByWorkspace = new HashMap<String, AttributeDefinitionEntry>();
        XmlSerializerUtil.copyBean(state, this);
    }


    public static class TypeDefinitionEntry {
        public Map<String, TypeDefinition> typeDefinitions = new HashMap<String, TypeDefinition>();
    }

    public static class AttributeDefinitionEntry {
        public Map<String, List<AttributeDefinition>> attributeDefinitions = new HashMap<String, List<AttributeDefinition>>();
    }

}
