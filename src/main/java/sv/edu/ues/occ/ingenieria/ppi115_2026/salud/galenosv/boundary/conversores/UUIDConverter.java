package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, Object> {

    @Override
    public Object convertToDatabaseColumn(UUID attribute) {
        return attribute;
    }

    @Override
    public UUID convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof UUID) {
            return (UUID) dbData;
        }
        if (dbData instanceof String) {
            return UUID.fromString((String) dbData);
        }
        throw new IllegalArgumentException("No se puede convertir " + dbData + " a UUID");
    }
}
