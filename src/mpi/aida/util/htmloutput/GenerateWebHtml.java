package mpi.aida.util.htmloutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javatools.parsers.Char;
import mpi.aida.access.DataAccess;
import mpi.aida.data.DisambiguationResults;
import mpi.aida.data.PreparedInput;
import mpi.aida.data.ResultEntity;
import mpi.aida.data.ResultMention;
import mpi.aida.util.Result;
import mpi.tokenizer.data.Token;
import mpi.tokenizer.data.Tokens;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import basics.Basics;
import basics.Normalize;

public class GenerateWebHtml {
  private static final Logger logger = 
      LoggerFactory.getLogger(GenerateWebHtml.class);

  private String javascriptArrayHoldingTypeInfo = "";

  private Set<String> prohibitedTypes = new HashSet<String>();

  private Map<String, Integer> typesCount = new HashMap<String, Integer>();

  public GenerateWebHtml() {
    prohibitedTypes.add("entity");
    prohibitedTypes.add("yagoLegalActorGeo");
    prohibitedTypes.add("yagoLegalActor");
  }

  public String process(String text, PreparedInput input, DisambiguationResults results, boolean generateTypeInformation) {
    if (results == null) {
      return "<div></div>";
    }
    Result result = new Result(input.getDocId(), text, input.getTokens(), "html");
    for (mpi.aida.data.ResultMention rm : results.getResultMentions()) {
      ResultEntity entity = results.getBestEntity(rm);
      String mentionString = rm.getMention();
      int charOffset = rm.getCharacterOffset();
      int charLength = rm.getCharacterLength();
      double confidence = entity.getDisambiguationScore();
      String entityName = entity.getEntity();
      mpi.aida.util.htmloutput.ResultMention rMention = 
          new mpi.aida.util.htmloutput.ResultMention(
              "html", charOffset, charLength, 
              mentionString, entityName, confidence, true);
      result.addFinalentity(rMention);
    }
    Map<String, List<String>> entitiesTypes;
    if(generateTypeInformation) {
    	entitiesTypes = loadEntitiesTypes(results);
    } else {
    	entitiesTypes = assignGenericType(results);
    }
    return toHtml(result, entitiesTypes);
  }

  private Map<String, List<String>> loadEntitiesTypes(DisambiguationResults results) {
    Set<String> entities = new HashSet<String>();
    DisambiguationResults disResults = results;
    for (ResultMention rm : disResults.getResultMentions()) {
      ResultEntity re = disResults.getBestEntity(rm);
      if (!re.isNoMatchingEntity()) {
        entities.add(re.getEntity());
      }
    }
    if (entities.size() > 0) {
      return DataAccess.getTypes(entities);
    }
    else {
      return new HashMap<String, List<String>>();
    }
  }

	private Map<String, List<String>> assignGenericType(
			DisambiguationResults results) {
		DisambiguationResults disResults = results;
		Map<String, List<String>> entitiesTypes = new HashMap<String, List<String>>();
		for (ResultMention rm : disResults.getResultMentions()) {
			ResultEntity re = disResults.getBestEntity(rm);
			List<String> entityTypes = new ArrayList<String>();
			entityTypes.add(Basics.ENTITY);
			entitiesTypes.put(re.getEntity(), entityTypes);
		}
		
		return entitiesTypes;
	}
  
  private String toHtml(Result result, Map<String, List<String>> entitiesTypes) {
    Map<String, List<String>> htmlSpanIdTypesMapping = new HashMap<String, List<String>>();
    logger.debug("Doing:" + result.getDocId());
    setTokens(result);
    Tokens tokens = result.getTokens();
    StringBuffer html = new StringBuffer();
    html.append("<div>\n");
    html.append(tokens.getOriginalStart());
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.getToken(i);
      if (token.containsData()) {
        int start = token.getId();
        token = tokens.getToken(start);
        mpi.aida.util.htmloutput.ResultMention mention = result.getMention(token.getBeginIndex()).get("html");
        int to = token.getId();
        int from = token.getId();
        String text = tokens.toText(from, to);
        while (!text.equalsIgnoreCase(mention.getMention()) && text.length() <= mention.getMention().length()) {
          to++;
          text = tokens.toText(from, to);
        }
        html.append("<small>[");
        if (mention.getEntity().equals("--NME--")) {
          html.append(mention.getEntity());
        } else {
          String name = Normalize.unEntity(mention.getEntity());
          String metaUriString = Char.encodeURIPathComponent(name);
          String displayString = Char.toHTML(Normalize.unNormalize(mention.getEntity()));
          double confidence = mention.getConfidence();
          html.append("<a title='" + confidence + "' target='_blank' href='http://en.wikipedia.org/wiki/" + metaUriString + "'>");
          html.append(displayString);
          html.append("</a>");
        }
        html.append("]</small>");
        String htmlSpanId = (mention.getMention() + "_" + from).replaceAll("[^a-zA-Z0-9]", "_");
        List<String> types = fixTypesName(entitiesTypes.get(mention.getEntity()));
        updateTypesCounts(types);
        htmlSpanIdTypesMapping.put(htmlSpanId, types);
        html.append("<span class='eq' id='" + htmlSpanId + "' title='" + StringUtils.join(types, " | ") + "'>");
        html.append("<a  class='links'>");
        html.append(text);
        html.append("</a></span>");
        token = tokens.getToken(to);
        html.append(token.getOriginalEnd());
        i = to;
      } else {
        html.append(token.getOriginal());
        html.append(token.getOriginalEnd());
      }
    }
    html.append("</div>\n");
    String typesJSONObjectsStr = getJSONObjects(htmlSpanIdTypesMapping);
    html.append(typesJSONObjectsStr);
    return html.toString();
  }

  private String getJSONObjects(Map<String, List<String>> htmlSpanIdTypesMapping) {

    Set<String> types = new HashSet<String>();
    for (List<String> typeSublist : htmlSpanIdTypesMapping.values()) {
      for (String type : typeSublist) {
        types.add(type);
      }
    }
    Map<String, Set<String>> typesIdsMap = new HashMap<String, Set<String>>();
    for (String id : htmlSpanIdTypesMapping.keySet()) {
      for (String type : htmlSpanIdTypesMapping.get(id)) {
        Set<String> ids = typesIdsMap.get(type);
        if (ids == null) {
          ids = new HashSet<String>();
          typesIdsMap.put(type, ids);
        }
        ids.add(id);
      }
    }

    StringBuilder out = new StringBuilder();
    for (String type : typesIdsMap.keySet()) {
      out.append("idsTypes['" + type.replaceAll("[^a-zA-Z0-9]", "") + "'] = ['" + StringUtils.join(typesIdsMap.get(type), "','") + "'];\n");
    }
    javascriptArrayHoldingTypeInfo = out.toString();
    out = new StringBuilder();

    out.append("<br/> <br/> <div id='typesListDiv' class='typesDiv' style='display:none'>");
    List<String> sortedTypes = new ArrayList<String>(types);
    Collections.sort(sortedTypes);
    for (String type : sortedTypes) {
      out.append("<span style='cursor: pointer' onclick='highlight(\"" + type.replaceAll("[^a-zA-Z0-9]", "") + "\")'>" + type + "</span> | ");
    }

    out.append("</div>");
    out.append(getTypeListString());
    return out.toString();
  }

  private void setTokens(Result result) {
    Tokens tokens = result.getTokens();
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.getToken(i);
      if (result.containsMention(token.getBeginIndex())) {
        HashMap<String, mpi.aida.util.htmloutput.ResultMention> mapping = result.getMention(token.getBeginIndex());
        Iterator<String> idsIter = mapping.keySet().iterator();
        while (idsIter.hasNext()) {
          String id = idsIter.next();
          mpi.aida.util.htmloutput.ResultMention mention = mapping.get(id);
          int to = token.getId();
          int from = token.getId();
          String text = tokens.toText(from, to);
          while (!text.equalsIgnoreCase(mention.getMention()) && text.length() <= mention.getMention().length()) {
            to++;
            text = tokens.toText(from, to);
          }
          for (int j = from; j <= to; j++) {
            tokens.getToken(j).addFinalEntity(id, mention.getEntity(), to - j);
          }
        }
      }
    }
  }

  private List<String> fixTypesName(List<String> types) {
    List<String> fixedTypes = new ArrayList<String>();
    if (types == null) return fixedTypes;
    for (int i = 0; i < types.size(); i++) {
      String type = types.get(i);
      String fixedType = Normalize.unNormalize(type);
      /*
      
      int start = type.indexOf("_") + 1;
      int end = type.lastIndexOf("_");
      
      System.out.println(type + "    " + start + "   " + end);
      if (start != -1 && end != -1 && start < end)
      	fixedType = type.substring(start, end).replace("_", " ");
      else
      	fixedType = type.replace("_", " ");*/
      fixedTypes.add(fixedType);
    }
    return fixedTypes;
  }

  private void updateTypesCounts(List<String> types) {
    Set<String> uniqueTypes = new HashSet<String>();
    for (String type : types) {
      uniqueTypes.add(type);
    }
    for (String type : uniqueTypes) {
      Integer count = typesCount.get(type);
      if (count == null) {
        typesCount.put(type, 1);
      } else {
        typesCount.put(type, count + 1);
      }
    }
  }

  private String getTypeListString() {
    StringBuilder out = new StringBuilder();
    out.append("<script type=\"text/javascript\">");
    out.append("typesList = [\n");
    int typesTotalCount = 0;
    for (String type : typesCount.keySet()) {
      if (prohibitedTypes.contains(type)) continue;
      typesTotalCount++;
      int count = typesCount.get(type);
      out.append("{text: '" + type.replaceAll("[^a-zA-Z0-9]", " ") + "', weight: " + count + ", title: \"" + type + ":" + count + "\", url: 'javascript:highlight(\"" + type.replaceAll("[^a-zA-Z0-9]", "") + "\");'},");
    }

    //		var word_list = [
    //		                 {text: "Lorem", weight: 15},
    //		                 {text: "Ipsum", weight: 9, url: "http://jquery.com/", title: "jQuery Rocks!"},
    //		                 {text: "Dolor", weight: 6},
    //		                 {text: "Sit", weight: 7},
    //		                 {text: "Amet", weight: 5}
    //		                 // ...other words
    //		             ];
    if (typesTotalCount > 0) {
      out.deleteCharAt(out.lastIndexOf(","));
    }
    out.append("];");
    out.append("</script>");
    return out.toString();
  }

  public String getJavascriptArrayHoldingTypeInfo() {
    return javascriptArrayHoldingTypeInfo;
  }
}
