package leap.orm.sql.ast;

import leap.lang.params.Params;
import leap.orm.sql.DynamicSqlClause;
import leap.orm.sql.SqlClause;
import leap.orm.sql.SqlFragment;
import leap.orm.sql.SqlStatementBuilder;
import leap.orm.sql.parser.SqlParserException;
import leap.orm.sql.parser.Token;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by KAEL on 2016/5/8.
 */
public class IncludeClause extends DynamicNode {

    protected String fragmentKey;

    public IncludeClause(String fragmentKey) {
        this.fragmentKey = fragmentKey;
    }

    @Override
    protected void toString_(Appendable buf) throws IOException {
        buf.append(Token.AT_INCLUDE.literal().toLowerCase());
        buf.append(Token.LPAREN.literal());
        buf.append(fragmentKey);
        buf.append(Token.RPAREN.literal());
    }

    @Override
    protected void buildStatement_(SqlStatementBuilder stm, Params params) throws IOException {
        SqlFragment fragment = stm.context().getOrmContext().getMetadata().getSqlFragment(fragmentKey);
        SqlClause[] sqlClauses = fragment.sqlClause();
        for(SqlClause sqlClause : sqlClauses){
            if(sqlClause instanceof DynamicSqlClause){
                AstNode[] nodes = ((DynamicSqlClause)sqlClause).getSql().nodes();
                for (AstNode node : nodes){
                    node.buildStatement(stm,params);
                }
            }
        }

    }
}
