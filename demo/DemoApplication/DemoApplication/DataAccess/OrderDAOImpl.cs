using Application.DataAccess;
using System.Collections.Generic;

namespace Application.DataAccess
{
	/**
	 * ///<summary>
	 * ///注文検索DAO実装
	 * ///</summary>
	 * 
	 */
	public class OrderDAOImpl : OrderDAO
	{
		/**
		 * ///<summary>
		 * ///注文検索
         * /// public IList_string[]_ findOrder()
         * /// になっていた。このコメントが反映されることを確認する。
		 * ///</summary>
		 * 
		 */
		public IList<string[]> findOrder()
		{
			return null;
		}

        /// <summary>
        /// 元のモデルにはコメントがなかった。
        /// このコメントの反映が成功することを確認する。
        /// </summary>
        /// <param name="data"></param>
		public void addOrder(string[] data)
		{

		}

	}

}

