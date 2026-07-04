package org.example.newcarre.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.newcarre.dto.JdClusterSentence;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

@Mapper
public interface KmeansSentence extends BaseMapper<JdClusterSentence> {
    @Insert("<script>" +
            "INSERT INTO jd_cluster_sentence(job_keyword,sentence_text) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.jobKeyword},#{item.sentenceText})" +
            "</foreach>" +
            "ON CONFLICT (job_keyword,sentence_text) DO NOTHING" +
            "</script>")
    int insertBatch(@Param("list") List<JdClusterSentence> list);


    // 1. 获取所有不重复的 job_keyword
    @Select("SELECT DISTINCT job_keyword FROM jd_cluster_sentence")
    List<String> findAllDistinctJobKeywords();

    // 2. 根据 job_keyword 获取所有句子
    @Select("SELECT * FROM jd_cluster_sentence WHERE job_keyword = #{jobKeyword}")
    List<JdClusterSentence> findByJobKeyword(String jobKeyword);

}
