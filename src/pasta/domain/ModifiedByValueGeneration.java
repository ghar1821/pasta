package pasta.domain;

import org.hibernate.Session;
import org.hibernate.tuple.AnnotationValueGeneration;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.ValueGenerator;

import pasta.domain.user.PASTAUser;
import pasta.web.WebUtils;


/* See http://docs.jboss.org/hibernate/orm/4.3/topical/html/generated/GeneratedValues.html */
public class ModifiedByValueGeneration
        implements AnnotationValueGeneration<ModifiedBy> {
    private final ValueGenerator<String> generator = new ValueGenerator<String>() {
        public String generateValue(Session session, Object owner) {
        	try {
            	PASTAUser user = WebUtils.getUser();
            	if(user == null) {
            		return "anonymous";
            	}
            	return user.getUsername();
            } catch(IllegalStateException e) {
            	return "pasta";
            }
        }
    };

    @Override
    public void initialize(ModifiedBy annotation, Class<?> propertyType) {
    }

    public GenerationTiming getGenerationTiming() {
        return GenerationTiming.ALWAYS;
    }

    public ValueGenerator<?> getValueGenerator() {
        return generator;
    }

    public boolean referenceColumnInSql() {
        return false;
    }

    public String getDatabaseGeneratedReferencedColumnValue() {
        return null;
    }
}
