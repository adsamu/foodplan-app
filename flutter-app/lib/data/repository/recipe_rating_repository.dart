import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:foodplan/data/remote/firestore_mappers.dart';
import 'package:foodplan/domain/model/recipe_rating.dart';

class RecipeRatingRepository {
  RecipeRatingRepository({required FirebaseFirestore firestore})
      : _firestore = firestore;

  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> _collection(String userId) =>
      _firestore.collection('users').doc(userId).collection('ratings');

  Stream<List<RecipeRating>> watchRatings(String userId) {
    return _collection(userId).snapshots().map(
          (snap) => snap.docs
              .map((doc) => doc.toRecipeRating())
              .whereType<RecipeRating>()
              .toList(),
        );
  }

  Future<void> saveRating(String userId, RecipeRating rating) =>
      _collection(userId)
          .doc(rating.recipeId)
          .set(rating.toFirestoreMap());
}
