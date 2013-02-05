package mpi.aida.util;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import mpi.aida.access.DataAccess;
import mpi.aida.data.Entities;
import mpi.aida.data.Entity;

import org.apache.commons.lang.StringUtils;

import basics.Normalize;

/**
 * This class contains some convenience wrappers for accessing YAGO data.
 * It has to use DataAccess and MUST NOT access the DB directly!
 * 
 * @author Johannes Hoffart
 *
 */
public class YagoUtil {
  
  public static final int TOTAL_YAGO_ENTITIES = 2651987;
  
  public enum Gender {
    FEMALE, MALE;
  }
 
  /**
   * Checks whether the given String is an entity in YAGO
   * 
   * @param entity  Entity to check.
   * @return        true if the entity is in YAGO
   * @throws SQLException
   */
  public static boolean isYagoEntity(Entity entity) throws SQLException {
    return DataAccess.isYagoEntity(entity);
  }
  
  public static Entity getEntityForId(int id) {
    return new Entity(DataAccess.getYagoEntityIdForId(id), id);
  }
  
  public static Entities getEntitiesForIds(int[] ids) {
    TIntObjectHashMap<String> yagoEntityIds =
        DataAccess.getYagoEntityIdsForIds(ids);
    Entities entities = new Entities();
    for (int i = 0; i < ids.length; ++i) {
      entities.add(new Entity(yagoEntityIds.get(ids[i]), ids[i]));
    }
    return entities;
  }
  
  public static Entity getEntityForYagoId(String id) {
    return new Entity(id, DataAccess.getIdForYagoEntityId(id));
  }
  
  public static Entities getEntitiesForYagoEntityIds(Collection<String> names) {
    Entities entities = new Entities();
    for (String name : names) {
      entities.add(new Entity(name, DataAccess.getIdForYagoEntityId(name)));
    }
    return entities;
  }
  
  /**
   * Formats a given mention string properly to query a yago database.
   * 
   * It will first transform the string into a YAGO string (with "" and
   * UTF-8 with backslash encoding), and then escape the string properly
   * for a Postgres query.
   * 
   * @param mention Mention to format
   * @return        Mention in YAGO2/Postgres format
   */
  public static String getYagoMentionStringPostgresEscaped(String mention) {
    return getPostgresEscapedString(Normalize.string(mention));
  }
  
  public static String getPostgresEscapedString(String input) {
    return input.replace("'", "''").replace("\\", "\\\\");
  }
  
  public static String getPostgresEscapedConcatenatedQuery(Collection<String> entities) {
    List<String> queryTerms = new LinkedList<String>();

    for (String term : entities) {
      StringBuilder sb = new StringBuilder();
      sb.append("E'").append(YagoUtil.getPostgresEscapedString(term)).append("'");
      queryTerms.add(sb.toString());
    }

    return StringUtils.join(queryTerms, ",");
  }
  
  public static String getIdQuery(TIntHashSet ids) {
    int[] conv = ids.toArray();
    return getIdQuery(conv);
  }
  
  public static String getIdQuery(int[] ids) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ids.length; ++i) {
      sb.append(ids[i]);
      if (i < ids.length - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }  
}