using System.Collections.Generic;

namespace Application.DataAccess
{
	/**
	 * ///<summary>
	 * ///注文検索DAO実装
	 * ///</summary>
	 */
	public class OrderDAOImpl : OrderDAO
	{
		/**
		 * ///<summary>
		 * ///注文検索
		 * ///</summary>
		 */
		public IList<string[]> findOrder()
		{
			return null;
		}

        /// <summary>
        /// 注文追加
        /// </summary>
        /// <param name="orderData">注文データ</param>
        public void addOrder(string[] orderData)
        {
            //モデルには存在しないメソッド。
            //生成されたコードに追加した。
            //「モデルへの反映に失敗した」ことがログに出力されることを確認する。
        }
	}

}

