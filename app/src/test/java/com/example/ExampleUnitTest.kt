package com.example

import com.example.data.entity.DailyTask
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun taskWithoutProof_hasProofReturnsFalse() {
    val task = DailyTask(
      taskKey = "SUNLIGHT",
      title = "Derazani och",
      category = "Ertalab",
      description = "10 daqiqa quyosh nuriga chiq",
      targetHour = 7,
      targetMinute = 0,
      date = "2026-09-29",
      isCompleted = false,
      proofPhotoUri = null
    )
    assertFalse(task.hasProof())
  }

  @Test
  fun taskWithProof_hasProofReturnsTrue() {
    val task = DailyTask(
      taskKey = "SUNLIGHT",
      title = "Derazani och",
      category = "Ertalab",
      description = "10 daqiqa quyosh nuriga chiq",
      targetHour = 7,
      targetMinute = 0,
      date = "2026-09-29",
      isCompleted = true,
      proofPhotoUri = "/data/user/0/com.example/files/task_proofs/proof_123.jpg"
    )
    assertTrue(task.hasProof())
    assertTrue(task.isCompleted)
  }
}

