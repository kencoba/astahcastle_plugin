using System.Collections.Generic;

namespace Application.DataAccess
{
    /// <summary>
    /// 元のモデルにはコメントがなかった。
    /// このコメントが反映されることを確認する。
    /// </summary>
	public interface OrderDAO
	{
		/**
		 * ///<summary>
		 * ///注文検索
         * /// メソッド定義が、以下の形になっていた。
         * ///IList_string[]_ findOrder()
         * /// このコメントが反映されることを確認する。
		 * ///</summary>
		 * 
		 */
		IList<string[]> findOrder();

	}

}

